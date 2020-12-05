package com.xtra.core.service;

import com.xtra.core.model.NetworkInterface;
import com.xtra.core.model.Resource;
import com.xtra.core.model.Server;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;

import java.util.ArrayList;
import java.util.List;


@Service
public class ServerService {
    private final MainServerApiService mainServerApiService;

    public ServerService(MainServerApiService mainServerApiService) {
        this.mainServerApiService = mainServerApiService;
    }

    public Resource getResourceUsage(String interfaceName) throws InterruptedException {

        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        CentralProcessor cpu = hal.getProcessor();

        long[][] oldProcTicks;
        oldProcTicks = new long[cpu.getLogicalProcessorCount()][CentralProcessor.TickType.values().length];
        double[] p = new double[cpu.getLogicalProcessorCount()];
        int i = 0;
        while (i< 2){
            p = cpu.getProcessorCpuLoadBetweenTicks(oldProcTicks);
            oldProcTicks = cpu.getProcessorCpuLoadTicks();
            Thread.sleep(400l);
            i++;
        }
        List<Float> currentUsage = new ArrayList<>();
        for (double item : p) {
            currentUsage.add( (float) item * 100);
        }

        GlobalMemory mem = hal.getMemory();
        NetworkInterface networkInterface = this.getNetworkInterfaceDetails(interfaceName, hal);

        Resource resource = new Resource(
                cpu.getMaxFreq()/1000000000000.0,
                currentUsage,
                mem.getTotal()/1000000000.0,
                mem.getAvailable()/1000000000.0,
                networkInterface.getName(),
                networkInterface.getBytesSent(),
                networkInterface.getBytesRecv(),
                si.getOperatingSystem().getSystemUptime()
                );

        return resource;
    }

    public NetworkInterface getNetworkInterfaceDetails(String interfaceName, HardwareAbstractionLayer hal){
        List<NetworkIF> networks =  hal.getNetworkIFs();
        NetworkInterface ntf = new NetworkInterface();
        networks.stream().filter(networkIF -> networkIF.getName().equals(interfaceName)).forEach(networkIF -> {
            ntf.setName(interfaceName);
            ntf.setBytesRecv(networkIF.getBytesRecv());
            ntf.setBytesSent(networkIF.getBytesSent());
        });
        return ntf;
    }

    public Server getServer(Long serverId) {
        try {
            return mainServerApiService.sendGetRequest("/servers/" + serverId, Server.class);
        } catch (RestClientException e) {
            //@todo log exception
            System.out.println(e.getMessage());
            return null;
        }
    }
}
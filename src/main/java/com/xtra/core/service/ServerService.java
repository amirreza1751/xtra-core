package com.xtra.core.service;

import com.xtra.core.model.NetworkInterface;
import com.xtra.core.model.Resource;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import java.util.ArrayList;
import java.util.List;


@Service
public class ServerService {

    public Resource getResourceUsage(String interfaceName) {

        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        CentralProcessor cpu = hal.getProcessor();
        long[][] oldProcTicks;
        oldProcTicks = new long[cpu.getLogicalProcessorCount()][CentralProcessor.TickType.values().length];
        double[] p = new double[cpu.getLogicalProcessorCount()];
        NetworkInterface newNetworkInterface = new NetworkInterface();
        int i = 0;
        while (i< 2){
            p = cpu.getProcessorCpuLoadBetweenTicks(oldProcTicks);
            oldProcTicks = cpu.getProcessorCpuLoadTicks();
            var oldNetworkInterface = getNetworkInterfaceDetails(interfaceName, hal);
            try {
                Thread.sleep(400L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            newNetworkInterface = getNetworkInterfaceDetails(interfaceName, hal);
            newNetworkInterface.setBytesRecv((newNetworkInterface.getBytesRecv() - oldNetworkInterface.getBytesRecv()) * 1000/ 400);
            newNetworkInterface.setBytesSent((newNetworkInterface.getBytesSent() - oldNetworkInterface.getBytesSent()) * 1000 / 400);
            i++;
        }
        List<Float> currentUsage = new ArrayList<>();
        for (double item : p) {
            currentUsage.add( (float) item * 100);
        }

        GlobalMemory mem = hal.getMemory();

        return new Resource(
                cpu.getMaxFreq()/1000000000000.0,
                currentUsage,
                mem.getTotal()/1000000000.0,
                mem.getAvailable()/1000000000.0,
                newNetworkInterface.getName(),
                newNetworkInterface.getBytesSent(),
                newNetworkInterface.getBytesRecv(),
                si.getOperatingSystem().getProcess((int) ProcessHandle.current().pid()).getUpTime()/1000
        );
    }

    public NetworkInterface getNetworkInterfaceDetails(String interfaceName, HardwareAbstractionLayer hal){
        List<NetworkIF> networks =  hal.getNetworkIFs();
        NetworkInterface ntf = new NetworkInterface();
        networks.stream().filter(networkIF -> networkIF.getName().equals(interfaceName)).forEach(networkIF -> {
            networkIF.updateAttributes();
            ntf.setName(interfaceName);
            ntf.setBytesRecv(networkIF.getBytesRecv());
            ntf.setBytesSent(networkIF.getBytesSent());
        });
        return ntf;
    }

}
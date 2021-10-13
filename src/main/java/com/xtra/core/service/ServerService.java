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

    long[] prevTicks = new long[CentralProcessor.TickType.values().length];
    public Resource getResourceUsage(String interfaceName) {

        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        CentralProcessor cpu = hal.getProcessor();
        double cpuLoad = cpu.getSystemCpuLoadBetweenTicks( prevTicks ) * 100;
        prevTicks = cpu.getSystemCpuLoadTicks();

        NetworkInterface newNetworkInterface = new NetworkInterface();
        int i = 0;
        while (i< 2){
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
        GlobalMemory mem = hal.getMemory();

        return new Resource(
                cpu.getMaxFreq()/1000000000000.0,
                cpuLoad,
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
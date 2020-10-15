package com.xtra.core.service;

import com.xtra.core.model.Cpu;
import com.xtra.core.model.Memory;
import com.xtra.core.model.NetworkInterface;
import com.xtra.core.model.Resource;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;

import java.util.List;


@Service
public class ServerService {

    public Resource getResourceUsage(String interfaceName){

        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        CentralProcessor cpu = hal.getProcessor();
        long[] currentFreq = cpu.getCurrentFreq();
        double[] currentFrequencies = new double[currentFreq.length];
        for(int i = 0; i < currentFreq.length; i++){
            currentFrequencies[i] = currentFreq[i]/1000000000.0;
        }
        GlobalMemory mem = hal.getMemory();

        Resource resource = new Resource();
        resource.setCpu(new Cpu(cpu.toString(), cpu.getMaxFreq()/1000000000000.0, currentFrequencies));
        resource.setMemory(new Memory(mem.toString(), mem.getTotal()/1000000000.0, mem.getAvailable()/1000000000.0));

        NetworkInterface networkInterface = this.getNetworkInterfaceDetails(interfaceName, hal);
        resource.setNetworkInterface(networkInterface);

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
}
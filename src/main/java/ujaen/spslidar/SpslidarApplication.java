package ujaen.spslidar;


import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import ujaen.spslidar.utils.properties.FileStorageProperties;
import ujaen.spslidar.utils.properties.LasToolsProperties;
import ujaen.spslidar.utils.properties.OctreeProperties;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

@SpringBootApplication
@EnableConfigurationProperties({FileStorageProperties.class, LasToolsProperties.class, OctreeProperties.class})
public class SpslidarApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpslidarApplication.class, args);
    }

    @Bean
    @Profile("docker")
    public void heapConfiguration(){
        int mb = 1024 * 1024;
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long xmx = memoryBean.getHeapMemoryUsage().getMax() / mb;
        long xms = memoryBean.getHeapMemoryUsage().getInit() / mb;
        System.out.println("Initial Memory (xms) :" + xms + " mb");
        System.out.println("Max Memory (xmx) :" + xmx + " mb");



    }


    /**
     * Micrometer initialization
     * @param registry
     * @return
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry){
        return new TimedAspect(registry);
    }

}

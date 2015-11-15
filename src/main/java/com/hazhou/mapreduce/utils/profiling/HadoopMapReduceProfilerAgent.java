package com.hazhou.mapreduce.utils.profiling;

import java.lang.instrument.Instrumentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HadoopMapReduceProfilerAgent {

	private static Logger logger = LoggerFactory.getLogger(HadoopMapReduceProfilerAgent.class);

    public static void premain(String agentArgs, Instrumentation inst) {
        logger.info("Registering instrumentation with agent args:{}", agentArgs);
        inst.addTransformer(new HadoopMapreduceProfilerClassFileTransformer(agentArgs));

    }
}

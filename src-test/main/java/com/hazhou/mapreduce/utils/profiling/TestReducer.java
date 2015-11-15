package com.hazhou.mapreduce.utils.profiling;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;

public class TestReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws java.io.IOException, InterruptedException {
  int sum = 0;
  TestService service = new TestService();
  for (IntWritable value : values) {
	service.testMethod();
    sum += value.get();
  }
  context.write(key, new IntWritable(sum));
}
	@Override
	public void cleanup(Context context){
		  System.out.println("Cleanup");
	}
}

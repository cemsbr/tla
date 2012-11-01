
package org.apache.hadoop.examples.ep2;

import java.io.IOException;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class Ep2 {
	
	private static ExperimentEntryProcessor entryProcessor;

	public static void main(String[] args) throws Exception {
		entryProcessor = new ExperimentEntryProcessor();
		
		
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		
		
		
		if (otherArgs.length != 2) {
			System.err.println("Usage: ep <in> <out>");
			System.exit(2);
		}
		
		
		@SuppressWarnings("deprecation")
		Job job = new Job(conf, "ep");
		job.setJarByClass(Ep2.class);
		job.setMapperClass(EpMapper.class);
		job.setCombinerClass(EpReducer.class);
		job.setReducerClass(EpReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
	
		
	
	
	/*
	 * 
	 * INNER Classes
	 * 
	 */
	public static class EpMapper extends Mapper<Object, Text, Text, IntWritable> {
		
		private Text lineKey = new Text();
		private IntWritable lineValue = new IntWritable();

		// called for each line 
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {			
			// splits each line
			String[] itr = value.toString().split(" ");
			
			// 4th string has format [time
			String timeString = itr[3].toString();
			
			// Object Date created with a substring of timeString (the substring without '[')
			Date date = new Date(Long.parseLong(timeString.substring(1)));
			
			// Search for experiment identity by date
			String params = entryProcessor.getParams(date.getTime());
			
			if( !params.isEmpty() ) {
				lineKey.set(itr[6].toString().concat(","+params));
				
				// write response time for this experiment instance
				lineValue.set(Integer.parseInt(itr[itr.length-1].toString()));
				
				context.write(lineKey, lineValue);
			}
		}
	}

	public static class EpReducer extends Reducer<Text,LongWritable,Text,LongWritable> {
		
		private LongWritable result = new LongWritable();

		public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
			
			int sum = 0;
			
			for (IntWritable val : values) {
				sum += val.get();
			}
			
			result.set(sum);
			context.write(key, result);
		}
	}
}

package org.apache.hadoop.examples.ep2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.ShortWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;


public class Ep2 extends Configured implements Tool {

	private static ExperimentEntryProcessor entryProcessor;

	public static void main(String[] args) throws Exception {
		ToolRunner.run(new Configuration(), new Ep2(), args);
	}

	@Override
	public int run(String[] args) throws Exception {
		
		if (args.length != 2) {
			System.err.println("Usage: ep2 <in> <out>");
			return 0;
		}
		
		entryProcessor = new ExperimentEntryProcessor();

		Configuration conf = getConf();

		@SuppressWarnings("deprecation")
		Job job = new Job(conf, "ep2");
		job.setJarByClass(Ep2.class);
		
		job.setMapperClass(EpMapper.class);
		job.setReducerClass(EpReducer.class);
		
		/*job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);*/
		
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(ShortWritable.class);
		
		Path outputpath = new Path(args[1]);

		FileInputFormat.addInputPath(job, new Path(args[0]));		
		FileOutputFormat.setOutputPath(job, outputpath);
		
		boolean result = job.waitForCompletion(true);

		return (result ? 0 : 1);
	}


	

	/*
	 * 
	 * INNER CLASSES
	 * 
	 */
	public static class EpMapper extends Mapper<Object, Text, Text, ShortWritable> {

		private Text lineKey = new Text();
		private ShortWritable lineValue = new ShortWritable();

		// called for each line 
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {			
			// splits each line
			String [] itr = value.toString().split(" ");

			// 4th string has format [time
			String timeString = itr[3].toString();
			
			// Object Date created with a substring of timeString (the substring without '[')
			Date date = entryProcessor.getDateInMillis(timeString.substring(1));
			if(date == null) return;

			// Search for experiment identity by date
			String params = entryProcessor.getParams(date.getTime());

			if( !params.isEmpty() ) {
				lineKey.set(itr[6].toString().concat(","+params));

				// write response time for this experiment instance
				lineValue.set(Short.parseShort(itr[itr.length-1].toString()));

				context.write(lineKey, lineValue);
			}
		}
	}

	public static class EpReducer extends Reducer<Text,ShortWritable,Text,Text> {

		private Text result = new Text();

		public void reduce(Text key, Iterable<ShortWritable> values, Context context) throws IOException, InterruptedException {

			int timeSum = 0;
			short count = 0;
			double mean = 0;
			double confidenceInterval = 0;
			double standardDeviation = 0;
			
			ArrayList<Short> times = new ArrayList<Short>();

			for (ShortWritable val : values) {
				// calc the sum of times
				timeSum += val.get();
				// calc the number of execution
				count++;
				// stores times
				times.add(val.get());
			}
			
			/*
			 * Mean
			 */
			mean = (double) timeSum / (double) count; 
			
			/*
			 * Standard deviation
			 */
			double squaredSum = 0.0;
			for(Short time : times) {
				squaredSum += ((time-mean)*(time-mean));
			}
			double variance = squaredSum / count;
			standardDeviation = Math.sqrt(variance);
			
			/*
			 * Confidence interval
			 */
			
			confidenceInterval = (1.96 * standardDeviation) / Math.sqrt(count);

			/*
			 * Write results
			 */
			result.set( "" + mean + "," + confidenceInterval );
			context.write(key, result);
		}
	}
}





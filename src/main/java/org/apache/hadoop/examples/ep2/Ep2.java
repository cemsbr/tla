
package org.apache.hadoop.examples.ep2;

import java.io.IOException;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.examples.WordMean;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
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
		ToolRunner.run(new Configuration(), new WordMean(), args);
	}

	@Override
	public int run(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println("Usage: wordmean <in> <out>");
			return 0;
		}

		Configuration conf = getConf();

		@SuppressWarnings("deprecation")
		Job job = new Job(conf, "word mean");
		job.setJarByClass(Ep2.class);
		job.setMapperClass(EpMapper.class);
		job.setReducerClass(EpReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(LongWritable.class);


		FileInputFormat.addInputPath(job, new Path(args[0]));
		Path outputpath = new Path(args[1]);
		FileOutputFormat.setOutputPath(job, outputpath);
		boolean result = job.waitForCompletion(true);


		/*
		 * Calc mean, standard deviation and confidence interval
		 */

		return (result ? 0 : 1);
	}


	

	/*
	 * 
	 * INNER CLASSES
	 * 
	 */
	public static class EpMapper extends Mapper<Object, Text, Text, LongWritable> {

		private Text lineKey = new Text();
		private LongWritable lineValue = new LongWritable();

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
				lineValue.set(Long.parseLong(itr[itr.length-1].toString()));

				context.write(lineKey, lineValue);
			}
		}
	}

	public static class EpReducer extends Reducer<Text,LongWritable,Text,LongWritable> {

		private LongWritable result = new LongWritable();

		public void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {

			int sum = 0;

			for (LongWritable val : values) {
				sum += val.get();
			}

			result.set(sum);
			context.write(key, result);
		}
	}
}

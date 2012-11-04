package br.usp.ime.tla;

import java.io.IOException;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
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

		if (args.length != 3) {
			System.err.println("Usage: ep2 <appLog> <in_path> <out_path>");
			return 0;
		}
		
		entryProcessor = new ExperimentEntryProcessor(args[0]);

		Configuration conf = getConf();
		
		FileSystem fs =  FileSystem.get(conf);
		
		/* Overwrite output dir if exists */
		fs.delete(new Path(args[2]), true);

		Job job = new Job(conf, "ep2");
		job.setJarByClass(Ep2.class);

		job.setMapperClass(EpMapper.class);
		job.setReducerClass(EpReducer.class);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(IntWritable.class);

		Path outputpath = new Path(args[2]);

		FileInputFormat.addInputPath(job, new Path(args[1]));
		FileOutputFormat.setOutputPath(job, outputpath);

		boolean result = job.waitForCompletion(true);
		
		return (result ? 0 : 1);
	}

	/*
	 * 
	 * INNER CLASSES
	 */
	public static class EpMapper extends
			Mapper<Object, Text, Text, IntWritable> {

		private Text lineKey = new Text();
		private IntWritable lineValue = new IntWritable();

		// called for each line
		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			
			if(!entryProcessor.matchesTomcatPattern(value.toString())) return;
			
			// splits each line
			String[] itr = value.toString().split(" ");

			// 4th string has format [time
			String timeString = itr[3].toString();

			// Object Date created with a substring of timeString (the substring
			// without '[')
			Date date = entryProcessor.getDateInMillis(timeString.substring(1));
			if (date == null)
				return;

			// Search for experiment identity by date
			String params = entryProcessor.getParams(date.getTime());

			if (!params.isEmpty()) {
				lineKey.set(itr[6].toString().concat("," + params));

				// write response time for this experiment instance
				lineValue.set(Integer.parseInt(itr[itr.length - 1].toString()));
				
				context.write(lineKey, lineValue);
			}
		}
	}

	public static class EpReducer extends
			Reducer<Text, IntWritable, Text, Text> {

		private Text result = new Text();

		public void reduce(Text key, Iterable<IntWritable> values,
				Context context) throws IOException, InterruptedException {

			final Statistics stats = new Statistics();

			for (IntWritable value : values) {
				stats.addValue(value.get());
			}

			final double mean = stats.getMean();
			final double confInterval = stats.getCI();

			/*
			 * Write results
			 */
			result.set("" + mean + "," + confInterval);
			context.write(key, result);
		}
	}
}

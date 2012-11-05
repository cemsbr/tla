package br.usp.ime.tla;

import java.io.IOException;
import java.text.ParseException;
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

public class TomcatLogAnalyzer extends Configured implements Tool {

	static ExperimentLogParser expParser;

	public static void main(String[] args) throws Exception {
		ToolRunner.run(new Configuration(), new TomcatLogAnalyzer(), args);
	}

	@Override
	public int run(String[] args) throws Exception {

		if (args.length != 3) {
			System.err.println("Usage: ep2 <appLog> <in_path> <out_path>");
			return 0;
		}

		expParser = new ExperimentLogParser(args[0]);

		Configuration conf = getConf();

		FileSystem fs = FileSystem.get(conf);
		/* Overwrite output dir if exists */
		fs.delete(new Path(args[2]), true);

		Job job = new Job(conf, "ep2");
		job.setJarByClass(TomcatLogAnalyzer.class);

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

		private final Text lineKey = new Text();
		private final IntWritable lineValue = new IntWritable();

		private final TomcatLogParser tomcatParser = new TomcatLogParser();
		private Date date;
		private String service, type;
		private int responseTime;

		// called for each line
		public void map(final Object key, final Text value,
				final Context context) throws IOException, InterruptedException {

			final String tomcatLine = value.toString();
			if (!tomcatParser.setLine(tomcatLine)) {
				return;
			}

			try {
				date = tomcatParser.getDate();
				service = tomcatParser.getServiceName();
				responseTime = tomcatParser.getResponseTime();
				type = expParser.getType(date.getTime());

				lineKey.set(service + "," + type);
				lineValue.set(responseTime);
				context.write(lineKey, lineValue);
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (ExperimentNotFoundException e) {
				e.printStackTrace();
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

package org.nts.pigutils.proto;

import java.io.IOException;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.pig.data.Tuple;

import com.google.protobuf.Message;

/**
 * 
 * RecordWriter that writes tuple(s) as ProtoBuff Base64 encoded LZO compressed
 * Line separated
 * 
 */
public class LzoProtobufRecordWriter extends
		RecordWriter<Writable, Tuple> {

	Path outputFile;

	//CompressionOutputStream lzoOut;
	//Compressor compressor;

	FSDataOutputStream fsDataOut;

	transient protected Message.Builder builder;
	transient protected final PigToProtobuf pigToProto = new PigToProtobuf();

	Class<? extends Message> protoClass;

	public LzoProtobufRecordWriter(Class<? extends Message> protoClass) {
		this.protoClass = protoClass;
	}

	public void init(TaskAttemptContext ctx, Path outputFile)
			throws IOException {
		// here we are requested to create the outuputFile and associate an
		// OutputStream for writting to it.
		// We do:
		// -> create Output file
		// -> create an LzopCodec and CompressionOutputStream
		// -> Wrap this with a Base64OutputStream

		this.outputFile = outputFile;

		FileSystem fs = outputFile.getFileSystem(ctx.getConfiguration());

		fsDataOut = fs.create(outputFile);

		//LzopCodec lzoCodec = new LzopCodec();
		//lzoCodec.setConf(ctx.getConfiguration());
		///compressor = CodecPool.getCompressor(lzoCodec);
		//lzoOut = lzoCodec.createOutputStream(fsDataOut, compressor);

	}

	@Override
	public void close(TaskAttemptContext ctx) throws IOException,
			InterruptedException {
		// Order is important
		// close the Base64OutputStream to flush any bytes it might have
		// call lzoOut.finish to assure completion of compression
		// close the lzo output stream
		// close the FSDataOutputStream
		//lzoOut.finish();
		//lzoOut.close();
		fsDataOut.close();

		// return the compressor resource
		//CodecPool.returnCompressor(compressor);
	}

	@Override
	public void write(Writable key, Tuple tuple) throws IOException,
			InterruptedException {

		// Convert the Tuple to a Message
		// encode the message bytes to base64
		// write the encoded message bytes to LzoOutputStream ->
		// FSDataOutputStream
		// write new line to LzoOutputStream -> FSDataOutputStream
		if (tuple != null) {
			final Message.Builder builder = Protobufs.getMessageBuilder(protoClass);
			//lzoOut.write(base64.encode(pigToProto
				//	.tupleToMessage(builder, tuple).toByteArray()));
			//lzoOut.write("\n".getBytes("UTF-8"));
			final byte[] bts = pigToProto.tupleToMessage(builder, tuple).toByteArray();
		    fsDataOut.writeInt(bts.length);
		    fsDataOut.write(bts);
		}

	}

}

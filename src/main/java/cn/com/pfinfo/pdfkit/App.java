package cn.com.pfinfo.pdfkit;


import com.beust.jcommander.JCommander;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.OperatorName;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDStream;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;


public class App {
	
	public static void main(String[] args){
		PDFCommandOptions op = new PDFCommandOptions();
		JCommander commander = JCommander.newBuilder().programName(AppCfg.APP_NAME)
				.addObject(op)
				.build();
		commander.parse(args);
		if (op.isHelp()){
			commander.usage();
		} else if (op.isVersion()) {
			System.out.printf("%s %s\n",AppCfg.APP_NAME,AppCfg.APP_VERSION);
		} else if (op.getKeyTokenNum()!=null){
			String inputPath = op.getFilePath();
			String mainName = inputPath.substring(0,inputPath.lastIndexOf('.'));
			removePdfWatermark(op.getFilePath(),
					mainName+"_new.pdf",op.getKeyPageNum(),op.getKeyTokenNum());
			System.out.println(mainName+"_new.pdf");
		} else {
			try {
				PDFWatermarkRemover.tryDoWatermarkRemove(op.getFilePath(),op.getKeyPageNum());
			} catch (IOException e) {
				e.printStackTrace();
				commander.usage();
			}
		}
	}

	/**
	 * @param sourPath    原pdf
	 * @param savePath    新pdf
	 * @param keyPageNum
	 * @param keyTokenNum
	 */
	public static void removePdfWatermark(String sourPath , String savePath, int keyPageNum, Integer keyTokenNum) {
		try {
			//读取源文件
			PDDocument helloDocument = PDDocument.load(new File(sourPath));
			PDPageTree allPages = helloDocument.getPages();
			PDPage keyPage = allPages.get(keyPageNum);
			PDFStreamParser parser = new PDFStreamParser(keyPage);
			parser.parse();
			Object keyToken = parser.getTokens().get(keyTokenNum);
			for(PDPage pdPage : allPages){
				parser = new PDFStreamParser(pdPage);
	            parser.parse();  
	            List<Object> tokens = parser.getTokens();
	            for (int j = 0; j < tokens.size(); j++) {
	                Object next = tokens.get(j);
					if (next.toString().equals(keyToken.toString())) {
						Operator EMPTY_TOKEN = Operator.getOperator(OperatorName.BEGIN_INLINE_IMAGE_DATA);
						tokens.set(j,EMPTY_TOKEN);
					}
	            }
	            PDStream updatedStream = new PDStream(helloDocument);
	            OutputStream out = updatedStream.createOutputStream();
	            ContentStreamWriter tokenWriter = new ContentStreamWriter(out);
	            tokenWriter.writeTokens(tokens);
	            pdPage.setContents(updatedStream);
				out.close();
			}
            //Output file name
			helloDocument.setAllSecurityToBeRemoved(true);
			helloDocument.save(savePath);
            helloDocument.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}


package cn.com.pfinfo.pdfkit;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import lombok.Getter;
import lombok.Setter;

import java.io.File;

@Setter
@Getter
public class PDFCommandOptions {
    @Parameter(names = {"-f", "-file-path"}, required = true, validateValueWith = FilePathValidator.class, description = "需要处理的pdf文件路径")
    private String filePath;
    @Parameter(names = {"-p", "-key-page"},
            validateWith = NumInteger.class,
            description = "关键页，通常选择pdf中包含水印的任意一页的页码，这个页码不是页面上显示逻辑页码，" +
                    "而是浏览器打开后顶部显示的当前页码。未指定时，使用默认值1")
    private int keyPageNum = 1;
    @Parameter(names = {"-t", "-key-token"}, validateWith = TokenInteger.class,
            description = "使用者在第一步操作后生成的pdf中确认水印消失的那一页的页码，这里的“页码”含义同key-page的含义一样。")
    private Integer keyTokenNum;

    @Parameter(names = {"help", "-help", "-h"},
            description = "查看帮助信息",
            help = true)
    private boolean help;

    @Parameter(names = {"version", "-version", "-v"},
            description = "显示当前版本号")
    private boolean version = false;

    public static class FilePathValidator implements IValueValidator<String> {

        @Override
        public void validate(String name, String value) throws ParameterException {
            File f = new File(value);
            if (!f.exists()) {
                throw new ParameterException("文件不存在：" + f.getAbsolutePath());
            } else if (!f.isFile()) {
                throw new ParameterException("它不是一个文件：" + f.getAbsolutePath());
            } else if (!f.canRead()) {
                throw new ParameterException("没有权限读取当前文件：" + f.getAbsolutePath());
            }
        }
    }

    public static class NumInteger implements IParameterValidator {

        @Override
        public void validate(String name, String value) throws ParameterException {
            int n = Integer.parseInt(value);
            if (n <= 0) {
                throw new ParameterException("Parameter " + name
                        + " should be great then zero (found " + value + ")");
            }
        }
    }

    public static class TokenInteger extends NumInteger {
        @Override
        public void validate(String name, String value) throws ParameterException {
            if (value == null || value.isEmpty()) {
                return;
            }
            super.validate(name, value);
        }
    }
}

package org.jeecgframework.codegenerate.generate.onetomany;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jeecgframework.codegenerate.generate.ICallBack;
import org.jeecgframework.codegenerate.util.CodeResourceUtil;
import org.jeecgframework.codegenerate.util.CodeStringUtils;

public class CodeFactoryOneToMany
{
  private ICallBack a;
  
  public enum CodeType
  {
	serviceImpl("ServiceImpl"),serviceI("ServiceI"),service("ServiceI"),jsp(""),jspList("List"),controller("Controller"),entity("Entity"),page("Page");
	private String jdField_a_of_type_JavaLangString;

    @SuppressWarnings("unused")
	private CodeType(String arg3)
    {
      Object localObject;
//      this.jdField_a_of_type_JavaLangString = localObject;
    	this.jdField_a_of_type_JavaLangString = arg3;
    }

    public String getValue()
    {
      return this.jdField_a_of_type_JavaLangString;
    }
  }

  @SuppressWarnings("deprecation")
public Configuration getConfiguration()
  {
    Configuration localConfiguration = new Configuration();
    String str = getTemplatePath();
    File localFile = new File(str);
    try{
    localConfiguration.setDirectoryForTemplateLoading(localFile);
    }catch(Exception e){
    	e.printStackTrace();
    }
    localConfiguration.setLocale(Locale.CHINA);
    localConfiguration.setDefaultEncoding("UTF-8");
    return localConfiguration;
  }

  @SuppressWarnings("rawtypes")
public void generateFile(String paramString1, String paramString2, Map paramMap)
  {
    try
    {
      String str1 = paramMap.get("entityPackage").toString();
      String str2 = paramMap.get("entityName").toString();
      String str3 = getCodePath(paramString2, str1, str2);
      String str4 = StringUtils.substringBeforeLast(str3, "/");
      Template localTemplate = getConfiguration().getTemplate(paramString1);
      FileUtils.forceMkdir(new File(str4 + "/"));
      OutputStreamWriter localOutputStreamWriter = new OutputStreamWriter(new FileOutputStream(str3), CodeResourceUtil.SYSTEM_ENCODING);
      localTemplate.process(paramMap, localOutputStreamWriter);
      localOutputStreamWriter.close();
    }
    catch (TemplateException localTemplateException)
    {
      localTemplateException.printStackTrace();
    }
    catch (IOException localIOException)
    {
      localIOException.printStackTrace();
    }
  }

  public String getProjectPath()
  {
    String str = System.getProperty("user.dir").replace("\\", "/") + "/";
    return str;
  }

  public String getClassPath()
  {
    String str = Thread.currentThread().getContextClassLoader().getResource("./").getPath();
    return str;
  }

  public static void main(String[] paramArrayOfString)
  {
    System.out.println(Thread.currentThread().getContextClassLoader().getResource("./").getPath());
  }

  public String getTemplatePath()
  {
    String str = getClassPath() + CodeResourceUtil.TEMPLATEPATH;
    return str;
  }

  public String getCodePath(String paramString1, String paramString2, String paramString3)
  {
    String str1 = getProjectPath();
    StringBuilder localStringBuilder = new StringBuilder();
    if (StringUtils.isNotBlank(paramString1))
    {
      String str2 = ((CodeFactoryOneToMany.CodeType)Enum.valueOf(CodeFactoryOneToMany.CodeType.class, paramString1)).getValue();
      localStringBuilder.append(str1);
      if (("jsp".equals(paramString1)) || ("jspList".equals(paramString1)))
        localStringBuilder.append(CodeResourceUtil.JSPPATH);
      else
        localStringBuilder.append(CodeResourceUtil.CODEPATH);
      if ("Action".equalsIgnoreCase(str2))
        localStringBuilder.append(StringUtils.lowerCase("action"));
      else if ("ServiceImpl".equalsIgnoreCase(str2))
        localStringBuilder.append(StringUtils.lowerCase("service/impl"));
      else if ("ServiceI".equalsIgnoreCase(str2))
        localStringBuilder.append(StringUtils.lowerCase("service"));
      else if (!"List".equalsIgnoreCase(str2))
        localStringBuilder.append(StringUtils.lowerCase(str2));
      localStringBuilder.append("/");
      localStringBuilder.append(StringUtils.lowerCase(paramString2));
      localStringBuilder.append("/");
      if (("jsp".equals(paramString1)) || ("jspList".equals(paramString1)))
      {
        String str3 = StringUtils.capitalize(paramString3);
        localStringBuilder.append(CodeStringUtils.getInitialSmall(str3));
        localStringBuilder.append(str2);
        localStringBuilder.append(".jsp");
      }
      else
      {
        localStringBuilder.append(StringUtils.capitalize(paramString3));
        localStringBuilder.append(str2);
        localStringBuilder.append(".java");
      }
    }
    else
    {
      throw new IllegalArgumentException("type is null");
    }
    return localStringBuilder.toString();
  }

  @SuppressWarnings("rawtypes")
public void invoke(String paramString1, String paramString2)
  {
    Object localObject = new HashMap();
    localObject = this.a.execute();
    generateFile(paramString1, paramString2, (Map)localObject);
  }

  public ICallBack getCallBack()
  {
    return this.a;
  }

  public void setCallBack(ICallBack paramICallBack)
  {
    this.a = paramICallBack;
  }
  
 
}

/* Location:           E:\Workspace\jeecg-framework-3.2.0.RELEASE\jeecg-v3-simple\WebRoot\WEB-INF\lib\org.jeecgframework.codegenerate.jar
 * Qualified Name:     org.jeecgframework.codegenerate.generate.onetomany.CodeFactoryOneToMany
 * JD-Core Version:    0.6.0
 */
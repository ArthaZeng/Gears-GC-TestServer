package info.gearsgc.webserver;

public interface GcFileManager {
	public void close();
	public String getUploadPath();
	
	public java.io.InputStream open(java.lang.String fileName) throws java.io.IOException ;
	
	public java.io.InputStream open(java.lang.String fileName, int accessMode) throws java.io.IOException;
	
	public boolean CreateDir(String subPath,String name);
	
	public boolean GetDir(String Path);
	
	public boolean DeleteFile(String Path);
	
}

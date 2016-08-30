package com.naskar.pls.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileDeleteOnCloseInputStream extends FileInputStream {
	
	private File file;

	public FileDeleteOnCloseInputStream(File file) throws FileNotFoundException {
		super(file);
		this.file = file;
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		file.delete();
	}

}

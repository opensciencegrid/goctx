package edu.iu.grid.tx.accessor;

import java.io.File;

public class Attachment {
	public String id; //unique attachment id
	public String name; //usually file name
	public String owner; //name of person who added this attachment
	public String content_type = null;//null means you need to guess from file. "application/octet-stream";
	public File file = null; //if the content is downloaded, this stores that.
}

package com.chrisrhoden.reader;

import java.util.List;

public class StreamItem {
	
	public long crawlTimeMsec;
	public long timestampUsec;
	public String id;
	public List<String> categories;
	public String title;
	public long published;
	public long updated;
	
	public List<StreamEnclosure> enclosure;
	public List<StreamEnclosure> alternate;
	public List<StreamLink> canonical;
	public List<User> likingUsers;
	public List<Comment> comments;
	public List<Annotation> annotations;
	
	public Summary summary;
	
	public class StreamEnclosure extends StreamLink {
		public String type;
		public long length;
	}
	
	public class Summary {
		public String direction;
		public String content;
	}
	
	public class User {}
	
	public class Comment {}
	
	public class Annotation {};
	
	public String toString() {
		return title;
	}

}

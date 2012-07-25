package com.chrisrhoden.reader.api;

import java.util.List;


public class StreamResponse {
	public String direction;
	public String id;
	public String title;
	public String continuation;
	public List<StreamLink> self;

	public String Author;

	public long updated;

	public List<StreamItem> items;
}

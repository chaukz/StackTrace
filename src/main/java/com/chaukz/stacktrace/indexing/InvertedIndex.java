package com.chaukz.stacktrace.indexing;

import com.chaukz.stacktrace.model.Posting;

import java.util.Hashtable;
import java.util.Map;

public class InvertedIndex {
Map<String, Map<Integer, Posting>> index = new Hashtable<>();

}

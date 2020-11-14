package org.coolreader.genrescollection;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class GenresCollection {

	private static final String TAG = "genre";

	static public final class GenreRecord {
		public String m_code;
		private String m_name;
		private List<GenreRecord> m_childs;
		private int m_level;
		private List<String> m_aliases;

		private GenreRecord(String code, String name, int level) {
			m_code = code;
			m_name = name;
			m_level = level;
			m_childs = new ArrayList<>();
			m_aliases = new ArrayList<>();
		}

		public String getCode() {
			return m_code;
		}

		public String getName() {
			return m_name;
		}

		public void setName(String name) {
			m_name = name;
		}

		public int getLevel() {
			return m_level;
		}

		public List<GenreRecord> getChilds() {
			return m_childs;
		}

		public boolean hasChilds() {
			return null != m_childs && !m_childs.isEmpty();
		}

		public boolean contain(String code) {
			if (m_code.equals(code))
				return true;
			for (GenreRecord record : m_childs) {
				if (record.getCode().equals(code))
					return true;
			}
			return false;
		}

		public boolean hasAliases() {
			return null != m_aliases && !m_aliases.isEmpty();
		}

		public List<String> getAliases() {
			return m_aliases;
		}

		private void addAlias(String alias) {
			boolean exist = false;
			for (String a : m_aliases) {
				if (a.equals(alias)) {
					exist = true;
					break;
				}
			}
			if (!exist) {
				m_aliases.add(alias);
			}
		}
	}

	// main container
	private Map<String, GenreRecord> m_collection;
	// key: genre code
	// value: genre record
	private Map<String, GenreRecord> m_allGenres;
	// instance
	private static GenresCollection m_instance = null;

	private GenresCollection() {
		m_collection = new LinkedHashMap<>();
		m_allGenres = new HashMap<>();
	}

	private void addGenre(String groupCode, String groupName, String code, String name) {
		GenreRecord group = m_collection.get(groupCode);
		if (null == group) {
			group = new GenreRecord(groupCode, groupName, 0);
			m_collection.put(groupCode, group);
			m_allGenres.put(groupCode, group);
		}
		List<GenreRecord> groupChilds = group.getChilds();
		// Check if already exist in this group
		GenreRecord exist = null;
		for (GenreRecord rec : groupChilds) {
			if (rec.getCode().equals(code)) {
				exist = rec;
				break;
			}
		}
		if (null == exist) {
			// add new
			GenreRecord record = new GenreRecord(code, name, 1);
			groupChilds.add(record);
			m_allGenres.put(code, record);
		} else {
			// update existing
			exist.setName(name);
		}
	}

	public GenreRecord byCode(String code) {
		return m_allGenres.get(code);
	}

	public String translate(String code) {
		GenreRecord record = m_allGenres.get(code);
		if (null != record)
			return record.getName();
		// If not found, return as is.
		return code;
	}

	public Map<String, GenreRecord> getCollection() {
		return m_collection;
	}

	public List<String> getGroups() {
		ArrayList<String> list = new ArrayList<>();
		for (Map.Entry<String, GenreRecord> entry : m_collection.entrySet()) {
			list.add(entry.getKey());
		}
		return list;
	}

	public static GenresCollection getInstance(Context context) {
		if (null == m_instance) {
			m_instance = new GenresCollection();
			m_instance.loadGenresFromResource(context);
		}
		return m_instance;
	}

	public static boolean reloadGenresFromResource(Context context) {
		if (null == m_instance) {
			m_instance = new GenresCollection();
			return m_instance.loadGenresFromResource(context);
		}
		return m_instance.loadGenresFromResource(context);
	}

	private boolean loadGenresFromResource(Context context) {
		boolean res = false;
		try {
			XmlResourceParser parser = context.getResources().getXml(R.xml.union_genres);
			// parser data
			Stack<String> tagStack = new Stack<>();
			String tag;
			String text = null;
			String parentTag;
			String groupCode = null;
			String groupName = null;
			String genreCode = null;
			String genreName = null;
			int count = 0;
			// genre aliases
			String aliasForCode = null;
			String aliasCode;
			HashMap<String, ArrayList<String>> allAliases = new HashMap<>();

			// start to parse
			parser.next();
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
					case XmlPullParser.START_DOCUMENT:
						//Log.d(TAG, "START_DOCUMENT");
						break;
					case XmlPullParser.START_TAG:
						tag = parser.getName();
						//Log.d(TAG, "START_TAG: " + tag);
						if (!tagStack.empty())
							parentTag = tagStack.peek();
						else
							parentTag = "";
						tagStack.push(tag);
						if ("genres".equals(tag)) {
							if (parentTag.length() == 0) {
								// root tag found, clearing genre's collection
								m_collection = new LinkedHashMap<>();
							} else {
								throw new XmlPullParserException("the element 'genres' must be the root element!");
							}
						} else if ("group".equals(tag)) {
							if ("genres".equals(parentTag)) {
								groupCode = parser.getAttributeValue(null, "code");
								groupName = parser.getAttributeValue(null, "name");
								if (null != groupName) {
									groupName = resolveStringResource(context, groupName);
								}
							} else {
								throw new XmlPullParserException("the 'group' element must only be inside the 'genres' element!");
							}
						} else if ("genre".equals(tag)) {
							if ("group".equals(parentTag)) {
								genreCode = parser.getAttributeValue(null, "code");
								genreName = parser.getAttributeValue(null, "name");
								if (null != genreName && genreName.length() > 0) {
									genreName = resolveStringResource(context, genreName);
								}
							} else {
								throw new XmlPullParserException("the 'genre' element must only be inside the 'group' element!");
							}
						} else if ("aliases".equals(tag)) {
							if ("genres".equals(parentTag)) {
								aliasForCode = null;
							} else {
								throw new XmlPullParserException("the 'aliases' element must only be inside the 'genres' element!");
							}
						} else if ("alias".equals(tag)) {
							if ("aliases".equals(parentTag)) {
								aliasForCode = parser.getAttributeValue(null, "code");
							} else {
								throw new XmlPullParserException("the 'alias' element must only be inside the 'aliases' element!");
							}
						}
						text = "";
						break;
					case XmlPullParser.END_TAG:
						//Log.d(TAG, "END_TAG: " + parser.getName());
						if (!tagStack.empty())
							tag = tagStack.pop();
						else
							tag = "";
						if (!tag.equals(parser.getName())) {
							throw new XmlPullParserException("end element '" + parser.getName() + "' not equal to start element '" + tag + "'");
						}
						if ("genre".equals(tag)) {
							if (null != groupCode && groupCode.length() > 0 &&
									null != groupName && groupName.length() > 0 &&
									null != genreCode && genreCode.length() > 0 &&
									null != genreName && genreName.length() > 0) {
								addGenre(groupCode, groupName, genreCode, genreName);
								count++;
							}
							genreCode = null;
							genreName = null;
						} else if ("group".equals(tag)) {
							groupCode = null;
							groupName = null;
						} else if ("alias".equals(tag)) {
							// save alias to temporary container
							aliasCode = text;
							if (null != aliasForCode && aliasForCode.length() > 0 &&
									null != aliasCode && aliasCode.length() > 0) {
								ArrayList<String> aliases = allAliases.get(aliasForCode);
								if (null == aliases) {
									aliases = new ArrayList<>();
									allAliases.put(aliasForCode, aliases);
								}
								// don't check already exist aliases here
								// checked in GenreRecord.addAlias()
								aliases.add(aliasCode);
							}
							aliasForCode = null;
						}
						break;
					case XmlPullParser.TEXT:
						//Log.d(TAG, "TEXT: " + parser.getText());
						text = parser.getText();
						break;
				}
				eventType = parser.next();
			}
			// Only after all genres are registered, add aliases
			for (Map.Entry<String, ArrayList<String>> entry : allAliases.entrySet()) {
				GenreRecord genre = byCode(entry.getKey());
				if (null != genre) {
					for (String alias : entry.getValue()) {
						genre.addAlias(alias);
					}
				} else {
					// do nothing
					// skip aliases for non-existent genres
					Log.w(TAG, "No such genre '" + entry.getKey() + "' to register aliases!");
				}
			}
			res = count > 0;
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (Resources.NotFoundException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return res;
	}

	private static String resolveStringResource(Context context, String resCode) {
		if (null != resCode && resCode.startsWith("@")) {
			try {
				int resId = Integer.parseInt(resCode.substring(1), 10);
				if (resId != 0) {
					String str = context.getString(resId);
					if (null != str && str.length() > 0)
						return str;
				}
			} catch (NumberFormatException ignored) {
				// ignore this exception, return original string
			}
		}
		return resCode;
	}
}
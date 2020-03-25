/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.tools.Resource;
import org.opensourcephysics.tools.ResourceLoader;

public class DataFile extends DataAdapter {
  java.util.List<Data> dataList = null;
  protected static String[] delimiters = new String[] {" ", "\t", ",", ";"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

  /**
   * Creates a DataFile using data in the given file.
   *
   * @param fileName
   */
  public DataFile(String fileName) {
    super(null);
    if(fileName!=null) {
      open(fileName);
    }
  }

  /**
   * Some objects (eg, a Group) do not contain data, but a list of Data
   * objects that do. This method is used by Data displaying tools to create
   * as many pages as needed.
   *
   * @return a list of Data objects, or null if this object contains data
   */
  public java.util.List<Data> getDataList() {
    return dataList;
  }

  /**
   * Opens an xml or data file specified by name.
   *
   * @param fileName   the file name
   * @return the file name, if successfully opened
   */
  public String open(String fileName) {
    dataList = null;
    data = null;
    OSPLog.fine("opening "+fileName); //$NON-NLS-1$
    Resource res = ResourceLoader.getResource(fileName);
    if(res!=null) {
      Reader in = res.openReader();
      String firstLine = readFirstLine(in);
      // if xml, read the file into an XML control and add tab
      if(firstLine.startsWith("<?xml")) { //$NON-NLS-1$
        XMLControlElement control = new XMLControlElement(fileName);
        dataList = control.getObjects(Data.class);
        return fileName;
      }
      // if not xml, attempt to import data and add tab
      else if(res.getString()!=null) {
        data = parseData(res.getString(), fileName);
        if(data!=null) {
          return fileName;
        }
      }
    }
    OSPLog.finest("no data found"); //$NON-NLS-1$
    return null;
  }

  /**
   * Parses character-delimited data from a string. This attempts to extract
   * the following information from the string:
   *
   * 1. A title to be used for the tab name 2. One or more columns of double
   * data values 3. Column names for the data columns
   *
   * @param dataString  the data string
   * @param fileName   name of file containing the data string (may be null)
   * @return DatasetManager with parsed data, or null if none found
   */
  public double[][] parseData(String dataString, String fileName) {
    BufferedReader input = new BufferedReader(new StringReader(dataString));
    String gnuPlotComment = "#"; //$NON-NLS-1$
    try {
      String textLine = input.readLine();
      for(int i = 0; i<DataFile.delimiters.length; i++) {
        ArrayList<double[]> rows = new ArrayList<double[]>();
        int columns = Integer.MAX_VALUE;
        String[] columnNames = null;
        String title = null;
        int lineCount = 0;
        while(textLine!=null) {                                      // process each line of text
          // look for gnuPlot-commented name and/or columnNames
          if(textLine.contains(gnuPlotComment)) {
            textLine = textLine.trim();
          }
          if(textLine.startsWith(gnuPlotComment)) {
            int k = textLine.indexOf("name:");                       //$NON-NLS-1$
            if(k>-1) {
              title = textLine.substring(k+5).trim();
            }
            k = textLine.indexOf("columnNames:");                    //$NON-NLS-1$
            if(k>-1) {
              textLine = textLine.substring(k+12).trim();
            } else {
              textLine = input.readLine();
              continue;
            }
          }
          // skip Vernier Format 2 header lines
          if((textLine.indexOf("Vernier Format")>-1                  //$NON-NLS-1$
            )||(textLine.indexOf(".cmbl")>-1)) {                     //$NON-NLS-1$
            textLine = input.readLine();
            continue;
          }
          String[] strings = DataFile.parseStrings(textLine, DataFile.delimiters[i]);
          double[] rowData = DataFile.parseDoubles(strings);
          // set null title if String[] length > 0, all entries
          // are NaN and only one entry is not ""
          if(rows.isEmpty()&&(strings.length>0)&&(title==null)) {
            String s = "";                                           //$NON-NLS-1$
            for(int k = 0; k<strings.length; k++) {
              if(Double.isNaN(rowData[k])&&!strings[k].equals("")) { //$NON-NLS-1$
                if(s.equals("")) {                                   //$NON-NLS-1$
                  s = strings[k];
                } else {
                  s = "";                                            //$NON-NLS-1$
                  break;
                }
              }
            }
            if(!s.equals("")) {                                      //$NON-NLS-1$
              title = s;
              textLine = input.readLine();
              continue;
            }
          }
          // set null column names if String[] length > 0,
          // all entries are NaN and none is ""
          if(rows.isEmpty()&&(strings.length>0)&&(columnNames==null)) {
            boolean valid = true;
            for(int k = 0; k<strings.length; k++) {
              if(!Double.isNaN(rowData[k])||strings[k].equals("")) { //$NON-NLS-1$
                valid = false;
                break;
              }
            }
            if(valid) {
              columnNames = strings;
              textLine = input.readLine();
              continue;
            }
          }
          // add double[] of length 1 or longer to rows
          if(strings.length>0) {
            lineCount++;
            boolean validData = true;
            boolean emptyData = true;
            for(int k = 0; k<strings.length; k++) {
              // invalid if any NaN entries other than ""
              if(Double.isNaN(rowData[k])&&!strings[k].equals("")) { //$NON-NLS-1$
                validData = false;
              }
              // look for empty row--every entry is ""
              if(!strings[k].equals("")) {                           //$NON-NLS-1$
                emptyData = false;
              }
            }
            if(rows.isEmpty()&&emptyData) {
              validData = false;
            }
            // add valid data, but ignore blank lines
            // that may precede real data
            if(validData) {
              rows.add(rowData);
              columns = Math.min(rowData.length, columns);
            }
          }
          // abort processing if no data found in first several lines
          if(rows.isEmpty()&&(lineCount>10)) {
            break;
          }
          textLine = input.readLine();
        }
        // create array if data found
        if(!rows.isEmpty()&&(columns>0)) {
          input.close();
          // first reassemble data from rows into columns
          double[][] dataArray = new double[columns][rows.size()];
          for(int row = 0; row<rows.size(); row++) {
            double[] next = rows.get(row);
            for(int j = 0; j<columns; j++) {
              dataArray[j][row] = next[j];
            }
          }
          setName((title==null) ? XML.getName(fileName) : title);
          this.setColumnNames(columnNames);
          OSPLog.finest("data found using delimiter \""              //$NON-NLS-1$
                        +DataFile.delimiters[i]+"\"");               //$NON-NLS-1$
          return dataArray;
        }
        // close the reader and open a new one
        input.close();
        input = new BufferedReader(new StringReader(dataString));
        textLine = input.readLine();
      }
    } catch(IOException e) {
      e.printStackTrace();
    }
    try {
      input.close();
    } catch(IOException ex) {
      ex.printStackTrace();
    }
    return null;
  }

  // ______________________________ protected methods ________________________
  protected String readFirstLine(Reader in) {
    BufferedReader input = null;
    if(in instanceof BufferedReader) {
      input = (BufferedReader) in;
    } else {
      input = new BufferedReader(in);
    }
    String openingLine;
    try {
      openingLine = input.readLine();
      while((openingLine==null)||openingLine.equals("")) { //$NON-NLS-1$
        openingLine = input.readLine();
      }
    } catch(IOException e) {
      e.printStackTrace();
      return null;
    }
    try {
      input.close();
    } catch(IOException ex) {
      ex.printStackTrace();
    }
    return openingLine;
  }

  /**
   * Parses a String into tokens separated by a specified delimiter. A token
   * may be "".
   *
   * @param text the text to parse
   * @param delimiter the delimiter
   * @return an array of String tokens
   */
  protected static String[] parseStrings(String text, String delimiter) {
    Collection<String> tokens = new ArrayList<String>();
    if(text!=null) {
      // get the first token
      String next = text;
      int i = text.indexOf(delimiter);
      if(i==-1) {   // no delimiter
        tokens.add(stripQuotes(next));
        text = null;
      } else {
        next = text.substring(0, i);
        text = text.substring(i+1);
      }
      // iterate thru the tokens and add to token list
      while(text!=null) {
        tokens.add(stripQuotes(next));
        i = text.indexOf(delimiter);
        if(i==-1) { // no delimiter
          next = text;
          tokens.add(stripQuotes(next));
          text = null;
        } else {
          next = text.substring(0, i).trim();
          text = text.substring(i+1);
        }
      }
    }
    return tokens.toArray(new String[0]);
  }

  /**
   * Parses a String into tokens separated by specified row and column
   * delimiters.
   *
   * @param text the text to parse
   * @param rowDelimiter the column delimiter
   * @param colDelimiter  the column delimiter
   * @return a 2D array of String tokens
   */
  protected static String[][] parseStrings(String text, String rowDelimiter, String colDelimiter) {
    String[] rows = parseStrings(text, rowDelimiter);
    String[][] tokens = new String[rows.length][0];
    for(int i = 0; i<rows.length; i++) {
      tokens[i] = parseStrings(rows[i], colDelimiter);
    }
    return tokens;
  }

  /**
   * Parses a String array into doubles. Unparsable strings are set to
   * Double.NaN.
   *
   * @param strings   the String array to parse
   * @return an array of doubles
   */
  protected static double[] parseDoubles(String[] strings) {
    double[] doubles = new double[strings.length];
    for(int i = 0; i<strings.length; i++) {
      if(strings[i].indexOf("\t")>-1) { //$NON-NLS-1$
        doubles[i] = Double.NaN;
      } else {
        try {
          doubles[i] = Double.parseDouble(strings[i]);
        } catch(NumberFormatException e) {
          doubles[i] = Double.NaN;
        }
      }
    }
    return doubles;
  }

  /**
   * Parses a String into doubles separated by specified row and column
   * delimiters.
   *
   * @param text  the text to parse
   * @param rowDelimiter the column delimiter
   * @param colDelimiter the column delimiter
   * @return a 2D array of doubles
   */
  protected static double[][] parseDoubles(String text, String rowDelimiter, String colDelimiter) {
    String[][] strings = parseStrings(text, rowDelimiter, colDelimiter);
    double[][] doubles = new double[strings.length][0];
    for(int i = 0; i<strings.length; i++) {
      double[] row = new double[strings[i].length];
      for(int j = 0; j<row.length; j++) {
        try {
          row[j] = Double.parseDouble(strings[i][j]);
        } catch(NumberFormatException e) {
          row[j] = Double.NaN;
        }
      }
      doubles[i] = row;
    }
    return doubles;
  }

  /**
   * Strips quotation marks around a string.
   *
   * @param text the text to strip
   * @return the stripped string
   */
  private static String stripQuotes(String text) {
    if(text.startsWith("\"")) {       //$NON-NLS-1$
      String stripped = text.substring(1);
      int n = stripped.indexOf("\""); //$NON-NLS-1$
      if(n==stripped.length()-1) {
        return stripped.substring(0, n);
      }
    }
    return text;
  }

  /**
   * Returns an array of row numbers.
   *
   * @param rowCount  length of the array
   * @return the array
   */
  protected static double[] getRowArray(int rowCount) {
    double[] rows = new double[rowCount];
    for(int i = 0; i<rowCount; i++) {
      rows[i] = i;
    }
    return rows;
  }

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.

 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be released
 * under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2017  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */

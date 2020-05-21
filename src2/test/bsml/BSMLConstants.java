/*
 * Copyright (C) 2003  Dr. Chris Upton, University of Victoria
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package test.bsml;

/**
 * this class holds constants that are useful when producing and reading bsml
 * documents
 *
 * @author Ryan Brodie
 * @version 1.0
 */
public final class BSMLConstants {
    //~ Static fields/initializers /////////////////////////////////////////////

    public static final String SEQ_ID_PREFIX = "SEQ-ID:";
    public static final String GENE_ID_PREFIX = "GENE-ID:";
    public static final String QUALIFIED_NAME = "Bsml";
    public static final String PUBLIC_ID = "-//Labbook, Inc. BSML DTD//EN";
    public static final String DTD_URI =
        "http://www.labbook.com/dtd/bsml3_1.dtd";
}

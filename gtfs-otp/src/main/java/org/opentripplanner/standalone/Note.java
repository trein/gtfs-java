/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package org.opentripplanner.standalone;

import javax.xml.bind.annotation.XmlElement;

public class Note {
	@XmlElement
	public String text;

	public Note() {
		/* Required by JAXB but unused */
	}
	
	public Note(String note) {
		text = note;
	}
	
	public boolean equals(Object o) {
		return (o instanceof Note) && ((Note) o).text.equals(text);
	}
	
	public int hashCode() {
		return text.hashCode();
	}
}
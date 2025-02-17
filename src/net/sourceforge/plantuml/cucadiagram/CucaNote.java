/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009-2023, Arnaud Roques
 *
 * Project Info:  http://plantuml.com
 * 
 * If you like this project or if you find it useful, you can support us at:
 * 
 * http://plantuml.com/patreon (only 1$ per month!)
 * http://plantuml.com/paypal
 * 
 * This file is part of PlantUML.
 *
 * PlantUML is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PlantUML distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 *
 * Original Author:  Arnaud Roques
 * 
 *
 */
package net.sourceforge.plantuml.cucadiagram;

import net.sourceforge.plantuml.command.Position;
import net.sourceforge.plantuml.graphic.color.Colors;

public class CucaNote {

	private final Display display;
	private final Position position;
	private final Colors colors;
	private final NoteLinkStrategy strategy;

	private CucaNote(Display display, Position position, Colors colors, NoteLinkStrategy strategy) {
		this.display = display;
		this.position = position;
		this.colors = colors;
		this.strategy = strategy;
	}

	public static CucaNote build(Display display, Position position, Colors colors) {
		return new CucaNote(display, position, colors, NoteLinkStrategy.NORMAL);
	}

	public CucaNote withStrategy(NoteLinkStrategy strategy) {
		return new CucaNote(display, position, colors, strategy);
	}

	public final Display getDisplay() {
		return display;
	}

	public final NoteLinkStrategy getStrategy() {
		return strategy;
	}

	public final Colors getColors() {
		return colors;
	}

	public final Position getPosition() {
		return position;
	}

}

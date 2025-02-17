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
package net.sourceforge.plantuml.creole;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.sourceforge.plantuml.LineBreakStrategy;
import net.sourceforge.plantuml.annotation.HaxeIgnored;
import net.sourceforge.plantuml.awt.geom.XDimension2D;
import net.sourceforge.plantuml.awt.geom.XRectangle2D;
import net.sourceforge.plantuml.creole.atom.Atom;
import net.sourceforge.plantuml.creole.legacy.StripeSimple;
import net.sourceforge.plantuml.graphic.AbstractTextBlock;
import net.sourceforge.plantuml.graphic.HorizontalAlignment;
import net.sourceforge.plantuml.graphic.InnerStrategy;
import net.sourceforge.plantuml.graphic.StringBounder;
import net.sourceforge.plantuml.graphic.TextBlock;
import net.sourceforge.plantuml.style.ClockwiseTopRightBottomLeft;
import net.sourceforge.plantuml.ugraphic.MinMax;
import net.sourceforge.plantuml.ugraphic.UGraphic;
import net.sourceforge.plantuml.ugraphic.UTranslate;

public class SheetBlock1 extends AbstractTextBlock implements TextBlock, Atom, Stencil {

	public List<Atom> splitInTwo(StringBounder stringBounder, double width) {
		throw new UnsupportedOperationException(getClass().toString());
	}

	private final Sheet sheet;
	private List<Stripe> stripes;
	private Map<Stripe, Double> heights;
	private Map<Stripe, Double> widths;
	private Map<Atom, Position> positions;
	private MinMax minMax;
	private final LineBreakStrategy maxWidth;
	private final ClockwiseTopRightBottomLeft padding;
	private final double marginX1;
	private final double marginX2;

	@HaxeIgnored
	public SheetBlock1(Sheet sheet, LineBreakStrategy maxWidth, double padding) {
		this(sheet, maxWidth, ClockwiseTopRightBottomLeft.same(padding), 0, 0);
	}

	public SheetBlock1(Sheet sheet, LineBreakStrategy maxWidth, ClockwiseTopRightBottomLeft padding) {
		this(sheet, maxWidth, padding, 0, 0);
	}

	public SheetBlock1(Sheet sheet, LineBreakStrategy maxWidth, double padding, double marginX1, double marginX2) {
		this(sheet, maxWidth, ClockwiseTopRightBottomLeft.same(padding), marginX1, marginX2);
	}

	public SheetBlock1(Sheet sheet, LineBreakStrategy maxWidth, ClockwiseTopRightBottomLeft padding, double marginX1,
			double marginX2) {
		this.sheet = sheet;
		this.maxWidth = Objects.requireNonNull(maxWidth);
		this.padding = padding;
		this.marginX1 = marginX1;
		this.marginX2 = marginX2;
	}

	@Override
	public String toString() {
		return sheet.toString();
	}

	public HorizontalAlignment getCellAlignment() {
		if (stripes.size() != 1)
			return HorizontalAlignment.LEFT;

		final Stripe simple = stripes.get(0);
		if (simple instanceof StripeSimple)
			return ((StripeSimple) simple).getCellAlignment();

		return HorizontalAlignment.LEFT;
	}

	private void initMap(StringBounder stringBounder) {
		if (positions != null)
			return;

		stripes = new ArrayList<>();
		for (Stripe stripe : sheet)
			stripes.addAll(new Fission(stripe, maxWidth).getSplitted(stringBounder));

		positions = new LinkedHashMap<>();
		widths = new LinkedHashMap<>();
		heights = new LinkedHashMap<>();
		minMax = MinMax.getEmpty(true);
		double y = 0;
		for (Stripe stripe : stripes) {
			if (stripe.getAtoms().size() == 0)
				continue;

			final Sea sea = new Sea(stringBounder);
			for (Atom atom : stripe.getAtoms())
				sea.add(atom);

			sea.doAlign();
			sea.translateMinYto(y);
			sea.exportAllPositions(positions);
			final double width = sea.getWidth();
			widths.put(stripe, width);
			minMax = sea.update(minMax);
			final double height = sea.getHeight();
			heights.put(stripe, height);
			y += height;
		}
		final int coef;
		if (sheet.getHorizontalAlignment() == HorizontalAlignment.CENTER)
			coef = 2;
		else if (sheet.getHorizontalAlignment() == HorizontalAlignment.RIGHT)
			coef = 1;
		else
			coef = 0;

		if (coef != 0) {
			double maxWidth = 0;
			for (Double v : widths.values())
				if (v > maxWidth)
					maxWidth = v;

			for (Map.Entry<Stripe, Double> ent : widths.entrySet()) {
				// final double diff = maxWidth - ent.getValue() + this.marginX1 +
				// this.marginX2;
				final double diff = maxWidth - ent.getValue();
				if (diff > 0) {
					for (Atom atom : ent.getKey().getAtoms()) {
						final Position pos = positions.get(atom);
						positions.put(atom, pos.translateX(diff / coef));
					}
				}

			}

		}
	}

	public XDimension2D calculateDimension(StringBounder stringBounder) {
		initMap(stringBounder);
		return XDimension2D.delta(minMax.getDimension(), padding.getBottom() + padding.getTop());
	}

	@Override
	public XRectangle2D getInnerPosition(String member, StringBounder stringBounder, InnerStrategy strategy) {
		return null;
	}

	public void drawU(UGraphic ug) {
		initMap(ug.getStringBounder());
		if (padding.getLeft() > 0 || padding.getTop() > 0)
			ug = ug.apply(new UTranslate(padding.getLeft(), padding.getTop()));

		for (Stripe stripe : stripes)
			for (Atom atom : stripe.getAtoms()) {
				final Position position = positions.get(atom);
				atom.drawU(position.translate(ug));
				// position.drawDebug(ug);
			}
	}

	public double getStartingAltitude(StringBounder stringBounder) {
		return 0;
	}

	public double getStartingX(StringBounder stringBounder, double y) {
		return -marginX1;
	}

	public double getEndingX(StringBounder stringBounder, double y) {
		return calculateDimension(stringBounder).getWidth() + marginX2;
	}

}

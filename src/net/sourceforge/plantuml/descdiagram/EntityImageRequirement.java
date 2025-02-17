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
 * Original Author:  Yijun Yu
 * 
 *
 */
package net.sourceforge.plantuml.descdiagram;

import net.sourceforge.plantuml.ColorParam;
import net.sourceforge.plantuml.FontParam;
import net.sourceforge.plantuml.Guillemet;
import net.sourceforge.plantuml.ISkinParam;
import net.sourceforge.plantuml.LineParam;
import net.sourceforge.plantuml.SkinParamUtils;
import net.sourceforge.plantuml.Url;
import net.sourceforge.plantuml.awt.geom.XDimension2D;
import net.sourceforge.plantuml.creole.Stencil;
import net.sourceforge.plantuml.cucadiagram.BodyFactory;
import net.sourceforge.plantuml.cucadiagram.Display;
import net.sourceforge.plantuml.cucadiagram.ILeaf;
import net.sourceforge.plantuml.cucadiagram.Stereotype;
import net.sourceforge.plantuml.graphic.FontConfiguration;
import net.sourceforge.plantuml.graphic.HorizontalAlignment;
import net.sourceforge.plantuml.graphic.StringBounder;
import net.sourceforge.plantuml.graphic.TextBlock;
import net.sourceforge.plantuml.graphic.TextBlockUtils;
import net.sourceforge.plantuml.graphic.color.ColorType;
import net.sourceforge.plantuml.style.SName;
import net.sourceforge.plantuml.style.Style;
import net.sourceforge.plantuml.style.StyleSignatureBasic;
import net.sourceforge.plantuml.svek.AbstractEntityImage;
import net.sourceforge.plantuml.svek.ShapeType;
import net.sourceforge.plantuml.ugraphic.AbstractUGraphicHorizontalLine;
import net.sourceforge.plantuml.ugraphic.TextBlockInEllipse;
import net.sourceforge.plantuml.ugraphic.UEllipse;
import net.sourceforge.plantuml.ugraphic.UGraphic;
import net.sourceforge.plantuml.ugraphic.UHorizontalLine;
import net.sourceforge.plantuml.ugraphic.UStroke;
import net.sourceforge.plantuml.ugraphic.UTranslate;
import net.sourceforge.plantuml.ugraphic.color.HColor;

public class EntityImageRequirement extends AbstractEntityImage {

	final private TextBlock desc;

	final private Url url;

	public EntityImageRequirement(ILeaf entity, ISkinParam skinParam) {
		super(entity, skinParam);
		final Stereotype stereotype = entity.getStereotype();

		final TextBlock tmp = BodyFactory.create2(skinParam.getDefaultTextAlignment(HorizontalAlignment.CENTER),
				entity.getDisplay(), skinParam, stereotype, entity, getStyle());

		if (stereotype == null || stereotype.getLabel(Guillemet.DOUBLE_COMPARATOR) == null) {
			this.desc = tmp;
		} else {
			final TextBlock stereo = Display.getWithNewlines(stereotype.getLabel(getSkinParam().guillemet())).create(
					FontConfiguration.create(getSkinParam(), FontParam.REQUIREMENT_STEREOTYPE, stereotype),
					HorizontalAlignment.CENTER, skinParam);
			this.desc = TextBlockUtils.mergeTB(stereo, tmp, HorizontalAlignment.CENTER);
		}
		this.url = entity.getUrl99();

	}
	
	private Style getStyle() {
		return StyleSignatureBasic.of(SName.root, SName.element, SName.componentDiagram, SName.requirement)
				.withTOBECHANGED(getEntity().getStereotype()).getMergedStyle(getSkinParam().getCurrentStyleBuilder());
	}


	private UStroke getStroke() {
		UStroke stroke = getSkinParam().getThickness(LineParam.requirementBorder, getStereo());
		if (stroke == null)
			stroke = new UStroke(7, 7, 1.5);

		return stroke;
	}

	public XDimension2D calculateDimension(StringBounder stringBounder) {
		return new TextBlockInEllipse(desc, stringBounder).calculateDimension(stringBounder);
	}

	final public void drawU(UGraphic ug) {
		final StringBounder stringBounder = ug.getStringBounder();
		final TextBlockInEllipse ellipse = new TextBlockInEllipse(desc, stringBounder);

		if (url != null)
			ug.startUrl(url);

		ug = ug.apply(getStroke());
		HColor linecolor = getEntity().getColors().getColor(ColorType.LINE);
		if (linecolor == null)
			linecolor = SkinParamUtils.getColor(getSkinParam(), getStereo(), ColorParam.requirementBorder);

		ug = ug.apply(linecolor);
		HColor backcolor = getEntity().getColors().getColor(ColorType.BACK);
		if (backcolor == null)
			backcolor = SkinParamUtils.getColor(getSkinParam(), getStereo(), ColorParam.requirementBackground);

		ug = ug.apply(backcolor.bg());
		final UGraphic ug2 = new MyUGraphicEllipse(ug, 0, 0, ellipse.getUEllipse());

		ellipse.drawU(ug2);

		if (url != null)
			ug.closeUrl();

	}

	public ShapeType getShapeType() {
		return ShapeType.OVAL;
	}

	static class MyUGraphicEllipse extends AbstractUGraphicHorizontalLine {

		private final double startingX;
		private final double yTheoricalPosition;
		private final UEllipse ellipse;

		@Override
		protected AbstractUGraphicHorizontalLine copy(UGraphic ug) {
			return new MyUGraphicEllipse(ug, startingX, yTheoricalPosition, ellipse);
		}

		MyUGraphicEllipse(UGraphic ug, double startingX, double yTheoricalPosition, UEllipse ellipse) {
			super(ug);
			this.startingX = startingX;
			this.ellipse = ellipse;
			this.yTheoricalPosition = yTheoricalPosition;
		}

		private double getNormalized(double y) {
			if (y < yTheoricalPosition)
				throw new IllegalArgumentException();

			y = y - yTheoricalPosition;
			if (y > ellipse.getHeight())
				throw new IllegalArgumentException();

			return y;
		}

		private double getStartingXInternal(double y) {
			return startingX + ellipse.getStartingX(getNormalized(y));
		}

		private double getEndingXInternal(double y) {
			return startingX + ellipse.getEndingX(getNormalized(y));
		}

		private Stencil getStencil2(UTranslate translate) {
			final double dy = translate.getDy();
			return new Stencil() {

				public double getStartingX(StringBounder stringBounder, double y) {
					return getStartingXInternal(y + dy);
				}

				public double getEndingX(StringBounder stringBounder, double y) {
					return getEndingXInternal(y + dy);
				}
			};
		}

		@Override
		protected void drawHline(UGraphic ug, UHorizontalLine line, UTranslate translate) {
			final UStroke stroke = new UStroke(1.5);
			line.drawLineInternal(ug.apply(translate), getStencil2(translate), 0, stroke);
		}

	}

}

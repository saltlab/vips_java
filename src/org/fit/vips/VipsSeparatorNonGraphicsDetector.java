/*
 * Tomas Popela, xpopel11, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - VipsSeparatorNonGraphicsDetector.java
 */

package org.fit.vips;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fit.cssbox.layout.TextBox;

public class VipsSeparatorNonGraphicsDetector implements VipsSeparatorDetector {

	VipsBlock _vipsBlocks = null;
	List<VipsBlock> _visualBlocks = null;
	private List<Separator> _horizontalSeparators = null;
	private List<Separator> _verticalSeparators = null;
	int _width = 0;
	int _height = 0;

	private boolean _cleanSeparators = false;

	/**
	 * Defaults constructor.
	 * @param width Pools width
	 * @param height Pools height
	 */
	public VipsSeparatorNonGraphicsDetector(int width, int height) {
		this._width = width;
		this._height = height;
		this._horizontalSeparators = new ArrayList<Separator>();
		this._verticalSeparators = new ArrayList<Separator>();
		this._visualBlocks = new ArrayList<VipsBlock>();
	}

	private void fillPoolWithBlocks(VipsBlock vipsBlock)
	{
		if (vipsBlock.isVisualBlock())
		{
			_visualBlocks.add(vipsBlock);
		}

		for (VipsBlock vipsBlockChild : vipsBlock.getChildren())
			fillPoolWithBlocks(vipsBlockChild);
	}

	/**
	 * Fills pool with all visual blocks from VIPS blocks.
	 * 
	 * @param vipsBlock
	 *            Visual block
	 */
	@Override
	public void fillPool()
	{
		fillPoolWithBlocks(_vipsBlocks);
	}

	/**
	 * Sets VIPS block, that will be used for separators computing.
	 * @param vipsBlock Visual structure
	 */
	@Override
	public void setVipsBlock(VipsBlock vipsBlock)
	{
		this._vipsBlocks = vipsBlock;
		_visualBlocks.clear();
		fillPoolWithBlocks(vipsBlock);
	}

	/**
	 * Gets VIPS block that is used for separators computing.
	 * @return Visual structure
	 */
	@Override
	public VipsBlock getVipsBlock()
	{
		return _vipsBlocks;
	}

	/**
	 * Sets VIPS block, that will be used for separators computing.
	 * @param vipsBlock Visual structure
	 */
	@Override
	public void setVisualBlocks(List<VipsBlock> visualBlocks)
	{
		this._visualBlocks.clear();
		this._visualBlocks.addAll(visualBlocks);
	}

	/**
	 * Gets VIPS block that is used for separators computing.
	 * @return Visual structure
	 */
	@Override
	public List<VipsBlock> getVisualBlocks()
	{
		return _visualBlocks;
	}

	/**
	 * Computes vertical visual separators from given visual blocks.
	 * @param vipsBlock Vips block
	 */
	private void findVerticalSeparators()
	{
		for (VipsBlock vipsBlock : _visualBlocks)
		{
			// block vertical coordinates
			int blockStart = vipsBlock.getBox().getAbsoluteContentX();
			int blockEnd = blockStart + vipsBlock.getBox().getContentWidth();

			// for each separator that we have in pool
			for (Separator separator : _verticalSeparators)
			{
				// find separator, that intersects with our visual block
				if (blockStart <= separator.endPoint)
				{
					// next there are six relations that the separator and visual block can have

					// if separator is inside visual block
					if (blockStart < separator.startPoint && blockEnd > separator.endPoint)
					{
						List<Separator> tempSeparators = new ArrayList<Separator>();
						tempSeparators.addAll(_verticalSeparators);

						//remove all separators, that are included in block
						for (Separator other : tempSeparators)
						{
							if (blockStart < other.startPoint && blockEnd > other.endPoint)
								_verticalSeparators.remove(other);
						}

						//find separator, that is on end of this block (if exists)
						for (Separator other : _verticalSeparators)
						{
							// and if it's necessary change it's start point
							if (blockEnd > other.startPoint && blockEnd < other.endPoint)
							{
								other.startPoint = blockEnd + 1;
								break;
							}
						}
						break;
					}
					// if block is inside another block -> skip it
					if (blockEnd < separator.startPoint)
						break;
					// if separator starts in the middle of block
					if (blockStart < separator.startPoint && blockEnd >= separator.startPoint)
					{
						// change separator start's point coordinate
						separator.startPoint = blockEnd+1;
						break;
					}
					// if block is in the middle of separator
					if (blockStart > separator.startPoint && blockEnd < separator.endPoint)
					{
						// add new separator that starts behind the block
						_verticalSeparators.add(_verticalSeparators.indexOf(separator) + 1, new Separator(blockEnd + 1, separator.endPoint));
						// change end point coordinates of separator, that's before block
						separator.endPoint = blockStart - 1;
						break;
					}
					// if in one block is one separator ending and another one starting
					if (blockStart > separator.startPoint && blockStart < separator.endPoint)
					{
						// find the next one
						int nextSeparatorIndex =_verticalSeparators.indexOf(separator);

						// if it's not the last separator
						if (nextSeparatorIndex + 1 < _verticalSeparators.size())
						{
							Separator nextSeparator = _verticalSeparators.get(_verticalSeparators.indexOf(separator) + 1);

							// next separator is really starting before the block ends
							if (blockEnd > nextSeparator.startPoint && blockEnd < nextSeparator.endPoint)
							{
								// change separator start point coordinate
								separator.endPoint = blockStart - 1;
								nextSeparator.startPoint = blockEnd + 1;
								break;
							}
						}
					}
					// if separator ends in the middle of block
					// change it's end point coordinate
					separator.endPoint = blockStart-1;
					break;
				}
			}
		}
	}

	/**
	 * Computes horizontal visual separators from given visual blocks.
	 * @param vipsBlock Vips Block
	 */
	private void findHorizontalSeparators()
	{
		for (VipsBlock vipsBlock : _visualBlocks)
		{
			// block vertical coordinates
			int blockStart = vipsBlock.getBox().getAbsoluteContentY();
			int blockEnd = blockStart + vipsBlock.getBox().getContentHeight();

			// for each separator that we have in pool
			for (Separator separator : _horizontalSeparators)
			{
				// find separator, that intersects with our visual block
				if (blockStart <= separator.endPoint)
				{
					// next there are six relations that the separator and visual block can have

					// if separator is inside visual block
					if (blockStart < separator.startPoint && blockEnd > separator.endPoint)
					{
						List<Separator> tempSeparators = new ArrayList<Separator>();
						tempSeparators.addAll(_horizontalSeparators);

						//remove all separators, that are included in block
						for (Separator other : tempSeparators)
						{
							if (blockStart < other.startPoint && blockEnd > other.endPoint)
								_horizontalSeparators.remove(other);
						}

						//find separator, that is on end of this block (if exists)
						for (Separator other : _horizontalSeparators)
						{
							// and if it's necessary change it's start point
							if (blockEnd > other.startPoint && blockEnd < other.endPoint)
							{
								other.startPoint = blockEnd + 1;
								break;
							}
						}
						break;
					}
					// if block is inside another block -> skip it
					if (blockEnd < separator.startPoint)
						break;
					// if separator starts in the middle of block
					if (blockStart <= separator.startPoint && blockEnd >= separator.startPoint)
					{
						// change separator start's point coordinate
						separator.startPoint = blockEnd+1;
						break;
					}
					// if block is in the middle of separator
					if (blockStart > separator.startPoint && blockEnd < separator.endPoint)
					{
						// add new separator that starts behind the block
						_horizontalSeparators.add(_horizontalSeparators.indexOf(separator) + 1, new Separator(blockEnd + 1, separator.endPoint));
						// change end point coordinates of separator, that's before block
						separator.endPoint = blockStart - 1;
						break;
					}
					// if in one block is one separator ending and another one starting
					if (blockStart > separator.startPoint && blockStart < separator.endPoint)
					{
						// find the next one
						int nextSeparatorIndex =_horizontalSeparators.indexOf(separator);

						// if it's not the last separator
						if (nextSeparatorIndex + 1 < _horizontalSeparators.size())
						{
							Separator nextSeparator = _horizontalSeparators.get(_horizontalSeparators.indexOf(separator) + 1);

							// next separator is really starting before the block ends
							if (blockEnd > nextSeparator.startPoint && blockEnd < nextSeparator.endPoint)
							{
								// change separator start point coordinate
								separator.endPoint = blockStart - 1;
								nextSeparator.startPoint = blockEnd + 1;
								break;
							}
						}
					}
					// if separator ends in the middle of block
					// change it's end point coordinate
					separator.endPoint = blockStart-1;
					break;
				}
			}
		}
	}

	/**
	 * Detects horizontal visual separators from Vips blocks.
	 */
	@Override
	public void detectHorizontalSeparators()
	{
		if (_visualBlocks.size() == 0)
		{
			System.err.println("I don't have any visual blocks!");
			return;
		}

		_horizontalSeparators.clear();
		_horizontalSeparators.add(new Separator(0, _height));

		findHorizontalSeparators();

		//remove pool borders
		List<Separator> tempSeparators = new ArrayList<Separator>();
		tempSeparators.addAll(_horizontalSeparators);

		for (Separator separator : tempSeparators)
		{
			if (separator.startPoint == 0)
				_horizontalSeparators.remove(separator);
			if (separator.endPoint == _height)
				_horizontalSeparators.remove(separator);
		}

		if (_cleanSeparators)
			cleanUpSeparators(_horizontalSeparators);
		computeHorizontalWeights();
		sortSeparatorsByWeight(_horizontalSeparators);
	}

	/**
	 * Detects vertical visual separators from Vips blocks.
	 */
	@Override
	public void detectVerticalSeparators()
	{
		if (_visualBlocks.size() == 0)
		{
			System.err.println("I don't have any visual blocks!");
			return;
		}

		_verticalSeparators.clear();
		_verticalSeparators.add(new Separator(0, _width));

		findVerticalSeparators();

		//remove pool borders
		List<Separator> tempSeparators = new ArrayList<Separator>();
		tempSeparators.addAll(_verticalSeparators);

		for (Separator separator : tempSeparators)
		{
			if (separator.startPoint == 0)
				_verticalSeparators.remove(separator);
			if (separator.endPoint == _width)
				_verticalSeparators.remove(separator);
		}

		if (_cleanSeparators)
			cleanUpSeparators(_verticalSeparators);
		computeVerticalWeights();
		sortSeparatorsByWeight(_verticalSeparators);
	}

	private void cleanUpSeparators(List<Separator> separators)
	{
		List<Separator> tempList = new ArrayList<>();
		tempList.addAll(separators);

		for (Separator separator : tempList)
		{
			int width = separator.endPoint - separator.startPoint;

			if (width < 10)
				separators.remove(separator);
		}

	}

	/**
	 * Sorts given separators by it's weight.
	 * @param separators Separators
	 */
	private void sortSeparatorsByWeight(List<Separator> separators)
	{
		Collections.sort(separators);
	}

	/**
	 * Computes weights for vertical separators.
	 */
	private void computeVerticalWeights()
	{
		for (Separator separator : _verticalSeparators)
		{
			ruleOne(separator);
			ruleTwo(separator, false);
			ruleThree(separator, false);
		}
	}

	/**
	 * Computes weights for horizontal separators.
	 */
	private void computeHorizontalWeights()
	{
		for (Separator separator : _horizontalSeparators)
		{
			ruleOne(separator);
			ruleTwo(separator, true);
			ruleThree(separator,true);
			ruleFour(separator);
			ruleFive(separator);
		}
	}

	/**
	 * The greater the distance between blocks on different
	 * side of the separator, the higher the weight. <p>
	 * For every 10 points of width we increase weight by 1 points.
	 * @param separator Separator
	 */
	private void ruleOne(Separator separator)
	{
		int width = separator.endPoint - separator.startPoint;
		int weight = 0;
		if (width < 10)
			weight = 1;
		else
			weight = (width / 10);

		separator.weight += weight;
	}

	/**
	 * If a visual separator is overlapped with some certain HTML
	 * tags (e.g., the &lt;HR&gt; HTML tag), its weight is set to be higher.
	 * @param separator Separator
	 */
	private void ruleTwo(Separator separator, boolean horizontal)
	{
		List<VipsBlock> overlappedElements = new ArrayList<VipsBlock>();
		if (horizontal)
			findHorizontalOverlappedElements(separator, _vipsBlocks, overlappedElements);
		else
			findVerticalOverlappedElements(separator, _vipsBlocks, overlappedElements);

		if (overlappedElements.size() == 0)
			return;

		for (VipsBlock vipsBlock : overlappedElements)
		{
			if (vipsBlock.getBox().getNode().getNodeName().equals("hr"))
			{
				separator.weight += 2;
				break;
			}
		}
	}

	/**
	 * Finds elements that are overlapped with horizontal separator.
	 * @param separator Separator, that we look at
	 * @param vipsBlock Visual block corresponding to element
	 * @param result Elements, that we found
	 */
	private void findHorizontalOverlappedElements(Separator separator,
			VipsBlock vipsBlock, List<VipsBlock> result)
	{
		int topEdge = vipsBlock.getBox().getAbsoluteContentY();
		int bottomEdge = topEdge + vipsBlock.getBox().getContentHeight();

		// two upper edges of element are overlapped with separator
		if (topEdge > separator.startPoint && topEdge < separator.endPoint && bottomEdge > separator.endPoint)
		{
			result.add(vipsBlock);
		}

		// two bottom edges of element are overlapped with separator
		if (topEdge < separator.startPoint && bottomEdge > separator.startPoint && bottomEdge < separator.endPoint)
		{
			result.add(vipsBlock);
		}

		// all edges of element are overlapped with separator
		if (topEdge >= separator.startPoint && bottomEdge <= separator.endPoint)
		{
			result.add(vipsBlock);
		}

		for (VipsBlock vipsBlockChild : vipsBlock.getChildren())
			findHorizontalOverlappedElements(separator, vipsBlockChild, result);
	}

	/**
	 * Finds elements that are overlapped with vertical separator.
	 * @param separator Separator, that we look at
	 * @param vipsBlock Visual block corresponding to element
	 * @param result Elements, that we found
	 */
	private void findVerticalOverlappedElements(Separator separator,
			VipsBlock vipsBlock, List<VipsBlock> result)
	{
		int leftEdge = vipsBlock.getBox().getAbsoluteContentX();
		int rightEdge = leftEdge + vipsBlock.getBox().getContentWidth();

		// two left edges of element are overlapped with separator
		if (leftEdge > separator.startPoint && leftEdge < separator.endPoint && rightEdge > separator.endPoint)
		{
			result.add(vipsBlock);
		}

		// two right edges of element are overlapped with separator
		if (leftEdge < separator.startPoint && rightEdge > separator.startPoint && rightEdge < separator.endPoint)
		{
			result.add(vipsBlock);
		}

		// all edges of element are overlapped with separator
		if (leftEdge >= separator.startPoint && rightEdge <= separator.endPoint)
		{
			result.add(vipsBlock);
		}

		for (VipsBlock vipsBlockChild : vipsBlock.getChildren())
			findVerticalOverlappedElements(separator, vipsBlockChild, result);
	}

	/**
	 * If background colors of the blocks on two sides of the separator
	 * are different, the weight will be increased.
	 * @param separator Separator
	 */
	private void ruleThree(Separator separator, boolean horizontal)
	{
		// for vertical is represents elements on left side
		List<VipsBlock> topAdjacentElements = new ArrayList<VipsBlock>();
		// for vertical is represents elements on right side
		List<VipsBlock> bottomAdjacentElements = new ArrayList<VipsBlock>();
		if (horizontal)
			findHorizontalAdjacentBlocks(separator, _vipsBlocks, topAdjacentElements, bottomAdjacentElements);
		else
			findVerticalAdjacentBlocks(separator, _vipsBlocks, topAdjacentElements, bottomAdjacentElements);

		if (topAdjacentElements.size() < 1 || bottomAdjacentElements.size() < 1)
			return;

		for (VipsBlock top : topAdjacentElements)
		{
			for (VipsBlock bottom : bottomAdjacentElements)
			{
				if (!top.getBgColor().equals(bottom.getBgColor()))
					separator.weight += 2;
			}
		}
	}

	/**
	 * Finds elements that are adjacent to horizontal separator.
	 * @param separator Separator, that we look at
	 * @param vipsBlock Visual block corresponding to element
	 * @param resultTop Elements, that we found on top side of separator
	 * @param resultBottom Elements, that we found on bottom side side of separator
	 */
	private void findHorizontalAdjacentBlocks(Separator separator,
			VipsBlock vipsBlock, List<VipsBlock> resultTop, List<VipsBlock> resultBottom)
	{
		if (vipsBlock.isVisualBlock())
		{
			int topEdge = vipsBlock.getBox().getAbsoluteContentY();
			int bottomEdge = topEdge + vipsBlock.getBox().getContentHeight();

			// if box is adjancent to separator from bottom
			if (topEdge == separator.endPoint + 1 && bottomEdge > separator.endPoint + 1)
			{
				resultBottom.add(vipsBlock);
			}

			// if box is adjancent to separator from top
			if (bottomEdge == separator.startPoint - 1 && topEdge < separator.startPoint - 1)
			{
				resultTop.add(0, vipsBlock);
			}
		}

		for (VipsBlock vipsBlockChild : vipsBlock.getChildren())
			findHorizontalAdjacentBlocks(separator, vipsBlockChild, resultTop, resultBottom);
	}

	/**
	 * Finds elements that are adjacent to vertical separator.
	 * @param separator Separator, that we look at
	 * @param vipsBlock Visual block corresponding to element
	 * @param resultLeft Elements, that we found on left side of separator
	 * @param resultRight Elements, that we found on right side side of separator
	 */
	private void findVerticalAdjacentBlocks(Separator separator,
			VipsBlock vipsBlock, List<VipsBlock> resultLeft, List<VipsBlock> resultRight)
	{
		if (vipsBlock.isVisualBlock())
		{
			int leftEdge = vipsBlock.getBox().getAbsoluteContentX() + 1;
			int rightEdge = leftEdge + vipsBlock.getBox().getContentWidth();

			// if box is adjancent to separator from right
			if (leftEdge == separator.endPoint + 1 && rightEdge > separator.endPoint + 1)
			{
				resultRight.add(vipsBlock);
			}

			// if box is adjancent to separator from left
			if (rightEdge == separator.startPoint - 1 && leftEdge < separator.startPoint - 1)
			{
				resultLeft.add(0, vipsBlock);
			}
		}
		for (VipsBlock vipsBlockChild : vipsBlock.getChildren())
			findVerticalAdjacentBlocks(separator, vipsBlockChild, resultLeft, resultRight);
	}

	/**
	 * For horizontal separators, if the differences of font properties
	 * such as font size and font weight are bigger on two
	 * sides of the separator, the weight will be increased.
	 * Moreover, the weight will be increased if the font size of the block
	 * above the separator is smaller than the font size of the block
	 * below the separator.
	 * @param separator Separator
	 */
	private void ruleFour(Separator separator)
	{
		List<VipsBlock> topAdjacentElements = new ArrayList<VipsBlock>();
		List<VipsBlock> bottomAdjacentElements = new ArrayList<VipsBlock>();

		findHorizontalAdjacentBlocks(separator, _vipsBlocks, topAdjacentElements, bottomAdjacentElements);

		if (topAdjacentElements.size() < 1 || bottomAdjacentElements.size() < 1)
			return;

		boolean weightIncreased = false;

		for (VipsBlock top : topAdjacentElements)
		{
			for (VipsBlock bottom : bottomAdjacentElements)
			{
				int diff = Math.abs(top.getFontSize() - bottom.getFontSize());
				if (diff != 0)
				{
					diff /= 2;
					separator.weight += 2;
					weightIncreased = true;
					break;
				}
			}
			if (weightIncreased)
				break;
		}

		weightIncreased = false;
		//TODO font weight

		for (VipsBlock top : topAdjacentElements)
		{
			for (VipsBlock bottom : bottomAdjacentElements)
			{
				if (top.getFontSize() < bottom.getFontSize())
				{
					separator.weight += 2;
					weightIncreased = true;
					break;
				}
			}
			if (weightIncreased)
				break;
		}
	}

	/**
	 * For horizontal separators, when the structures of the blocks on the two
	 * sides of the separator are very similar (e.g. both are text),
	 * the weight of the separator will be decreased.
	 * @param separator Separator
	 */
	private void ruleFive(Separator separator)
	{
		List<VipsBlock> topAdjacentElements = new ArrayList<VipsBlock>();
		List<VipsBlock> bottomAdjacentElements = new ArrayList<VipsBlock>();

		findHorizontalAdjacentBlocks(separator, _vipsBlocks, topAdjacentElements, bottomAdjacentElements);

		if (topAdjacentElements.size() < 1 || bottomAdjacentElements.size() < 1)
			return;

		boolean weightDecreased = false;

		for (VipsBlock top : topAdjacentElements)
		{
			for (VipsBlock bottom : bottomAdjacentElements)
			{
				if (top.getBox() instanceof TextBox &&
						bottom.getBox() instanceof TextBox)
				{
					separator.weight -= 2;
					weightDecreased = true;
					break;
				}
			}
			if (weightDecreased)
				break;
		}
	}

	/**
	 * @return the _horizontalSeparators
	 */
	@Override
	public List<Separator> getHorizontalSeparators()
	{
		return _horizontalSeparators;
	}

	@Override
	public void setHorizontalSeparators(List<Separator> separators)
	{
		_horizontalSeparators.clear();
		_horizontalSeparators.addAll(separators);
	}

	@Override
	public void setVerticalSeparators(List<Separator> separators)
	{
		_verticalSeparators.clear();
		_verticalSeparators.addAll(separators);
	}

	/**
	 * @return the _verticalSeparators
	 */
	@Override
	public List<Separator> getVerticalSeparators()
	{
		return _verticalSeparators;
	}

	@Override
	public void setCleanUpSeparators(boolean cleanSeparators)
	{
		this._cleanSeparators = cleanSeparators;
	}

	@Override
	public boolean isCleanUpEnabled()
	{
		return _cleanSeparators;
	}
}
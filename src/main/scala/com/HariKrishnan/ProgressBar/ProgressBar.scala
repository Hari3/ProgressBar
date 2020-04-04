package com.HariKrishnan.ProgressBar

/**
 * A Class to display a progress bar in the console.
 * The progress bar is shown as a collection of ASCII characters.
 * Example:  50.00% |███████       |  7/14
 *
 * 'Example:' is actually customizable, by setting `prefix`
 * Here, '|' is called `headChar`, '█' is called the `doneChar`, ' ' is the `leftChar` and `|` is the tailChar
 * These characters are used to customize the progress bar.
 *
 * an `edgeChar` is also available, which is displayed instead of the last `doneChar`
 *
 * Here is another progress bar with a different `edgeChar` defined:
 * Example: 50.00% [======>       ] 7/14
 * This is achieved by calling calling the parameterized constructor as new ProgressBar('[', '=', '>', ' ', ']')
 *
 */
class ProgressBar(headChar: Char, doneChar: Char, edgeChar: Char, leftChar: Char, tailChar: Char) {

  /*
   * Used instead of the Chars to support String Multiply functionality
   */
  private val headString = "" + headChar
  private val doneString = "" + doneChar
  private val edgeString = "" + edgeChar
  private val leftString = "" + leftChar
  private val tailString = "" + tailChar


  private var precision = 2
  private var prefix = ""
  private var max: Int = _
  private var desiredWidth = getConsoleWidth
  private var width: Int = 100
  private var left: Int = _
  private var unit: Double = _
  private var actualDone: Double = 0d

  private var done = 0
  setMax(100)

  // Default, parameter less Constructor
  def this() = this('|', '█', '█', ' ', '|')

  // If `edgeChar` isn't provided, it is assumed to the same as `doneChar`
  def this(headChar: Char, doneChar: Char, leftChar: Char, tailChar: Char) =
    this(headChar, doneChar, doneChar, leftChar, tailChar)

  /**
   * Set the maximum for the progressbar.
   * When progress reaches this number, the progress is complete.
   *
   * @param max : the value to be set as maximum
   */
  def setMax(max: Int): Unit = {
    this.max = max
    adjustWidth()
  }

  def setPrefix(prefix: String): Unit = {
    this.prefix = prefix
    adjustWidth()
  }

  private def adjustWidth(): Unit = {
    /*
    `setWidth` sets the `width`, which tracks how many steps are required to complete the progress
    The printed progress bar will contain at least these many characters, one for each step.
    To accommodate the various extra characters on both the left and right of the actual progress bar,
    several quantities are subtracted. The logic is as follows:
      * First compute the width of the console. The length of the printed progress bar should equal this.
      * From this subtract the numbers of characters required to display the `prefix`
      * Next, percentage completed is displayed.
      * This is always has
          * Three characters before the decimal point,            (3)
          * One decimal point,                                    (1)
          * `precision` number of characters after the point,     (precision)
          * and two more characters, '%' and a space              (2)
      * This amounts to subtracting precision and a 6.
      * Next, the `headChar` is displayed, thus 1 more is subtracted. Along with the previous 6, this amounts to 7.
      * Next the actual Progress Bar is displayed. The width of this is what we are actually calculating
      * Next, the `tailChar` is displayed, thus 1 more is subtracted. Along with the previous 7, this amounts to 8.
      * Next we have a space, subtracting one more, so we are now subtracting 9.
      * Next we display the completed steps over the total.
      * When displaying a number, we need as many characters as the number of it's digits.
      * In base 10, we know the number of digits is floor(log_10(number)) + 1. since we are displaying two numbers,
      * 2 times that quantity is subtracted.
      * The +1 inside is factored out as +2 and added to the running constant total to result in 11.
      * Note that while the completed steps might have less digits that total steps,
      * it is still padded with enough spaces to make them match.
      * While displaying completed steps over total, '/' is used, which amounts to another character.
      * Along with the previous 11, this amounts to 12 extra characters.
      * And finally, we display a carriage return. This is what makes the progress bar work, over writing the next line.
      * the carriage return character still counts as having width 1, so it is added to 12 we have previously calculated.
    All of this amounts to 13 extra characters.
     */
    val width = desiredWidth - prefix.length - precision - 2 * math.floor(math.log10(max)).toInt - 13
    unit = width.toDouble / max.toDouble
    actualDone = actualDone / this.width * width
    left = width - done
    this.width = width
  }

  /**
   * Set the display width of the progress bar.
   * Display scales by some factor to maintain actual maximum steps to be `max`
   *
   * @param w : The width to be set
   */
  def setWidth(w: Int): Unit = {
    desiredWidth = w
    adjustWidth()
  }

  private def getConsoleWidth: Int = {
    val w = jline.TerminalFactory.get().getWidth
    if (w == 0) max else w
  }

  def setPrecision(precision: Int): Unit = {
    this.precision = precision
    adjustWidth()
  }

  /**
   * Increase the progress by given number of steps.
   * Defaults to increase the progress by one step.
   * Internally calls `show`
   *
   * @param n : the amount by which to increase progress. Optional, defaults to 1.
   */
  def step(n: Int = 1): Unit = {
    if (done + n > width) {
      //adds a new line to console.
      //makes sure the exception dump occurs on a new line, since `show` uses \r.
      println()
      throw new ProgressLeakException(s"A step of $n was encountered, with ${round(done / unit).toInt} steps completed. " +
        s"This would exceed the set maximum of $max steps")
    }

    actualDone = actualDone + (n * unit)

    done = round(actualDone).toInt
    left = width - done
    show()
  }

  /**
   * Show the progress bar.
   * Called internally from `step`, so usually it is never required that you call this
   */
  def show(): Unit = {
    print(generateProgressBar())
  }

  /**
   * Private method to generate the progress bar to be printed.
   * Written as a method to enable unit testing.
   * Called from `show`
   *
   * @return a set of ASCII Characters, when printed on console represents the current progress
   */
  private def generateProgressBar(): String = {
    var progress: String = prefix
    progress = progress + padLeft(s"%.${precision}f%% ".format(actualDone / unit), 6 + precision)
    progress = progress + headString
    if (left == 0 || doneChar == edgeChar)
      progress = progress + (doneString * done)
    else
      progress = progress + (doneString * (done - 1)) + edgeString
    progress = progress + (leftString * left)
    progress = progress + tailString
    progress = progress + padLeft(round(actualDone / unit).toInt, Math.log10(max).toInt + 2) + "/" + max + "\r"

    progress
  }

  private def padLeft(input: Any, targetLength: Int): String = {
    if (input.toString.length >= targetLength) input.toString
    var output = input.toString
    while (output.length != targetLength) {
      output = ' ' + output
    }
    output
  }

  private def round(n: Double): Double = {
    val s = math.pow(10, precision)
    math.round(n * s) / s
  }


}

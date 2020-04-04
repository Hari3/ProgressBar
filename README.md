# ProgressBar
A simple module to display a progress bar on the console

The progress bar is shown as a collection of ASCII characters.
<pre>
Example: 50.00% |███████       | 7/14
</pre>
`Example:` is actually customizable, by setting `prefix`  
Here,
* `|` is called `headChar`
* `█` is called the `doneChar`
* ` ` (space) is the `leftChar`
* `|` is the tailChar

These characters are used to customize the progress bar.  
An `edgeChar` is also available, which is displayed instead of the last `doneChar`

Here is another progress bar with a different `edgeChar` defined:
<pre>
Example: 50.00% [======>       ] 7/14
</pre>
This is achieved by calling calling the parameterized constructor as:
<pre>
new ProgressBar('[', '=', '>', ' ', ']')
</pre>
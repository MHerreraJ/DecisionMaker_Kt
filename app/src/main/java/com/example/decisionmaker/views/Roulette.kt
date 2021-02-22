package com.example.decisionmaker.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.collections.ArrayList
import kotlin.math.*
import kotlin.random.Random

class Roulette : View {
    private var rouletteColors: ArrayList<Paint> = ArrayList()
    private var rouletteOptions: ArrayList<String> = ArrayList()
    private var highlightPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var chooserPaint:Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var rouletteOrientation = 0f

    private var startColor = arrayOf(255, 0, 0)
    private var endColor = arrayOf(255, 200, 0)

    private var tSize = 0

    var onRouletteViewListener:OnRouletteViewListener? = null

    constructor(context: Context?) : super(context) {
        init(null)
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs)
    }

    /**
     * Function for initializing the class (avoids repeated code in constructor)
     * @param attrs is the attribute set that describes the view
     */
    private fun init(attrs: AttributeSet?) {
        setPaintBrush()
        setBackgroundColor(Color.TRANSPARENT)
        highlightPaint.color = Color.WHITE
        highlightPaint.textSize = 70f
        highlightPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        highlightPaint.strokeWidth = 5f

        chooserPaint.color = Color.LTGRAY

        tSize = getTextBoxOffset("o").second
    }

    /**
     * This function calculates the paint brushes for coloring
     * the roulette, giving a degraded effect
     */
    private fun setPaintBrush(){
        rouletteColors.clear()

        if(rouletteOptions.size <= 1){
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.color = Color.rgb((startColor[0] + endColor[0])/2, (startColor[1] + endColor[1])/2, (startColor[2] + endColor[2])/2)
            rouletteColors.add(paint)

            return
        }

        val clr = arrayOf(0,0,0)
        for(i in 0 until rouletteOptions.size){
            //Get color by linear interpolation between start and end colors
            for(x in 0 until 3){
                clr[x] = startColor[x] + ((endColor[x]-startColor[x])*i)/(rouletteOptions.size-1)
            }
            val p = Paint(Paint.ANTI_ALIAS_FLAG)
            p.color = Color.rgb(clr[0], clr[1], clr[2])
            rouletteColors.add(p)
        }

    }

    /**
     * Overrides View onDraw fxn
     * @param canvas is the surface provided to draw.
     */
    override fun onDraw(canvas: Canvas) {
        //Get canvas center
        val cx = width/2.0f
        val cy = height/2.0f

        val n = rouletteOptions.size

        //Get roulette shape max dimension and radius
        val dim = min(width, height).toFloat()
        val radius = dim/2

        val triangle = Path()
        triangle.moveTo(cx + 0f, cy - radius + 10)
        triangle.lineTo(cx + 30f, cy - radius - 30)
        triangle.lineTo(cx - 30f, cy - radius - 30)
        triangle.lineTo(cx + 0f, cy - radius + 10)
        triangle.close()

        //Empty roulette, draw single circle with advice text
        if(n == 0) {
            val t1Off = getTextBoxOffset("Ruleta")
            val t2Off = getTextBoxOffset("vacia")
            canvas.drawCircle(cx, cy, radius, rouletteColors[0])
            canvas.drawText("Ruleta", cx - t1Off.first, cy - (t1Off.second*1.5).toInt(), highlightPaint)
            canvas.drawText("vacia", cx - t2Off.first, cy + (t2Off.second*1.5).toInt(), highlightPaint)
            canvas.drawPath(triangle, chooserPaint)
            return
        }

        canvas.save()
        canvas.rotate(rouletteOrientation-90, cx, cy)

        //Arc box: left, top corner
        val ax = (width - dim)/2
        val ay = (height - dim)/2

        //Arc box: right, bottom corner
        val dimx = dim + ax
        val dimy = dim + ay

        //Arc angle
        val alfa = 360.0f / n
        var t = -alfa/2.0f

        //Draw choices as 'pieces of cake'
        for(i in 0 until n){
            canvas.drawArc(ax, ay, dimx, dimy, t, alfa, true, rouletteColors[i])
            t +=alfa
        }

        t = -alfa/2.0f
        //Draw lines for choices separation
        if(n > 1) {
            for (i in 0 until n) {
                val lx = radius * cos(Math.toRadians(t.toDouble())).toFloat()
                val ly = radius * sin(Math.toRadians(t.toDouble())).toFloat()
                canvas.drawLine(cx, cy, cx + lx, cy + ly, highlightPaint)
                t += alfa
            }
        }

        //Draw choices text
        if(n > 1) { //Multiple options
            for (choice in rouletteOptions) {
                canvas.drawText(choice, cx + dim / 8, cy + tSize, highlightPaint)
                canvas.rotate(alfa, cx, cy)
            }
        }else{ //Single choice, draw centered text
            val xOff = getTextBoxOffset(rouletteOptions[0]).first
            canvas.drawText(rouletteOptions[0], cx - xOff, cy, highlightPaint)
        }
        canvas.restore()
        canvas.drawPath(triangle, chooserPaint)
    }

    private var animationStarted = false

    /**
     * Start spin animation, random values handled inside this methos
     * @param ms is the animation duration in milliseconds
     */
    fun spin(ms:Long){
        if(!animationStarted){
            animationStarted = true
            val tEnd = ms / 1000f
            val k = 9/tEnd

            val b0 = rouletteOrientation
            val randomRot = 360f/k*(14f + 2.3f*Random.nextFloat())
            val animator = ValueAnimator.ofFloat(0f, tEnd)
            animator.duration = ms

            animator.addUpdateListener {
                val t = it.animatedValue as Float
                val b = randomRot * (1 - exp(-0.8f*t*k))
                rouletteOrientation = (b0 + b)%360
                invalidate()
            }
            animator.addListener(spinAnimationListener)
            animator.start()
        }
    }

    /**
     * Spin animation listener, implements the onAnimationEnd listener
     * calculates the choice index, depending on roulette orientation
     */
    val spinAnimationListener = object:Animator.AnimatorListener{
        override fun onAnimationEnd(animation: Animator?) {
            animationStarted = false //Free flag for starting another animation process
            if(rouletteOptions.size == 0) return //Empty roulette
            if(rouletteOptions.size == 1){ //Only one option, choose it
                onRouletteViewListener?.OnRouletteSpinCompleted(0, rouletteOptions[0])
                return
            }


            val n = rouletteOptions.size
            rouletteOrientation %= 360f //Valid ranges between 0 and 360 degrees
            val step = 360.0f/n //Angle step size

            //Get index
            val idx = (floor(n - (rouletteOrientation - 0.5*step) / step) %n).toInt()
            Log.d("SPINCOMPLETED", idx.toString() + " " + rouletteOptions[idx])

            //Call spin completed listener
            onRouletteViewListener?.OnRouletteSpinCompleted(idx,rouletteOptions[idx])
        }

        override fun onAnimationStart(animation: Animator?) {}
        override fun onAnimationCancel(animation: Animator?) {}
        override fun onAnimationRepeat(animation: Animator?) {}
    }

    /**
     * Set the Roulette Option list and redraw
     * @param options is the option list
     */
    fun setRouletteOptionList(options:ArrayList<String>){
        rouletteOptions = options
        setPaintBrush()
        invalidate()
    }

    /**
     * Get text bounds depending on highlightPaint textSize,
     * then calculate the x and y relative centers
     * @param txt is the text string for calculating its bounds
     * @return Pair(x:Int,y:Int)
     */
    private fun getTextBoxOffset(txt:String) : Pair<Int, Int>{
        val bounds = Rect()
        highlightPaint.getTextBounds(txt, 0, txt.length, bounds)
        return Pair(bounds.width()/2, bounds.height()/2)
    }

}



package com.example.handyratingbar

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator



/**
 *
 * Flexible rating bar with animation
 *
 * @author Alex Galiatkin
 *
 * @property mInactiveStar - drawable for inactive star state
 *
 * @property mActiveStar - drawable for active star state
 *
 * @property mNumStars - count of all stars in rating
 * can be only from 3 to 10
 *
 * @property rating - current rating
 *
 * @property hasAnimation - if view will be animated
 *
 * @property mSpaceBetweenStars - if we need more space
 * between stars, this value in dp
 *
 * @see init
 * initiation of each values from resources
 *
 * @see createAnimator
 * this method will create animator, which will update
 * all stars, calls invalidate() method
 *
 * @see setRating
 * method for set new value for rating
 *
 * @see getRating
 * getter for rating
 *
 * @see getDrawableByResId
 * helper method which helps find drawable
 *
 * @see setRatingListener
 *
 * @see updateRating
 * private method for updating rating
 *
 */
class HandyRatingBar : View {


    /*
     flag, if view will have animation
     */
    private var hasAnimation = false

    /*
      drawables for active and inactive
      states
     */
    private var mInactiveStar: Drawable? = null
    private var mActiveStar: Drawable? = null


    private var mNumStars: Int = 0
    private var rating: Float = 0.toFloat()
    private var mStarSize: Int = 0
    private var verticalGravityOffset: Int = 0


    private var ratingAnimator: ValueAnimator? = null
    private var ratingListener: OnRatingBarChangeListener? = null
    private val animationDuration = 250

    //max value for animation
    private val offsetForTransformation = 4


    //space between each stars
    private var mSpaceBetweenStars: Int = 0

    //this is additional size, that we get when animation works
    private var sizeTransformedByAnimation: Int = 0


    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) { init(attrs) }

    private fun init(attrs: AttributeSet?) {

        val resources = this.resources
        val typedArray = this.context.obtainStyledAttributes(attrs, R.styleable.HandyRatingBar)


        this.mInactiveStar = typedArray.getDrawable(R.styleable.HandyRatingBar_inActiveStar)
        if (this.mInactiveStar == null) {
            this.mInactiveStar = this.getDrawableByResId(R.drawable.baseline_star_border_black_24)
        }

        this.mActiveStar = typedArray.getDrawable(R.styleable.HandyRatingBar_activeStar)
        if (this.mActiveStar == null) {
            this.mActiveStar = this.getDrawableByResId(R.drawable.baseline_star_black_24)
        }


        this.mNumStars = typedArray.getInt(R.styleable.HandyRatingBar_numStars, 5)
        if (this.mNumStars < 3) {
            this.mNumStars = 3
        } else if (this.mNumStars > 10) {
            this.mNumStars = 10
        }

        this.rating = typedArray.getFloat(R.styleable.HandyRatingBar_rating, 3f)
        if (this.rating > mNumStars) {
            this.rating = 3f
        }


        this.hasAnimation = typedArray.getBoolean(R.styleable.HandyRatingBar_hasAmimation, false)

        this.mSpaceBetweenStars = typedArray.getDimensionPixelSize(R.styleable.HandyRatingBar_starSpace, resources.getDimensionPixelSize(R.dimen.default_start_space))

        typedArray.recycle()


        ratingAnimator = createAnimator()
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {


        val measuredWidth = View.MeasureSpec.getSize(widthMeasureSpec)
        val measuredHeight = View.MeasureSpec.getSize(heightMeasureSpec)
        val heightOfPadding = paddingTop + paddingBottom
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)

        // offset for right and left sides
        val leftEndRightOffset = 2 * offsetForTransformation


        val allSpacesBetweenStarsSize: Int
        val height: Int


        //if height is exactly
        if (heightMode == View.MeasureSpec.EXACTLY) {


            height = measuredHeight

            mStarSize = measuredHeight - heightOfPadding - leftEndRightOffset

            if (mStarSize * mNumStars > measuredWidth - leftEndRightOffset) {
                // offset from all dividers between stars
                allSpacesBetweenStarsSize = mSpaceBetweenStars * (mNumStars - 1)

                //size of one star
                mStarSize = (measuredWidth - allSpacesBetweenStarsSize - leftEndRightOffset) / mNumStars

                verticalGravityOffset = (height - mStarSize - heightOfPadding - leftEndRightOffset) / 2

            } else {
                allSpacesBetweenStarsSize = measuredWidth - mStarSize * mNumStars
                mSpaceBetweenStars = allSpacesBetweenStarsSize / (mNumStars - 1)
            }


        } else {
            // offset from all dividers between stars
            allSpacesBetweenStarsSize = mSpaceBetweenStars * (mNumStars - 1)

            //size of one star
            mStarSize = (measuredWidth - allSpacesBetweenStarsSize - leftEndRightOffset) / mNumStars

            //height calculations
            height = mStarSize + heightOfPadding + leftEndRightOffset
        }


        setMeasuredDimension(measuredWidth, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)


        //begin from small offset
        var indicatorStartX = offsetForTransformation

        for (i in 0 until this.mNumStars) {

            val star = if (i < this.rating) this.mActiveStar else this.mInactiveStar


            if (i < rating) {
                star?.setBounds(
                        indicatorStartX - sizeTransformedByAnimation,
                        offsetForTransformation + verticalGravityOffset - sizeTransformedByAnimation,
                        indicatorStartX + this.mStarSize + sizeTransformedByAnimation,
                        this.mStarSize + (offsetForTransformation + verticalGravityOffset) + sizeTransformedByAnimation
                )
            } else {
                star?.setBounds(
                        indicatorStartX,
                        offsetForTransformation + verticalGravityOffset,
                        indicatorStartX + this.mStarSize,
                        this.mStarSize + (offsetForTransformation + verticalGravityOffset)
                )
            }



            indicatorStartX += mStarSize


            //if it's not last star, we will add space
            if (i != mNumStars - 1) {
                indicatorStartX += mSpaceBetweenStars
            }


            star?.draw(canvas)

        }
    }


    fun getRating(): Float {
        return rating + 1
    }


    private fun updateRating(x: Float) {
        rating = mNumStars.toFloat() / width * x
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {

        if (ratingAnimator != null && ratingAnimator!!.isRunning) {
            return true
        }

        val action = event.action and MotionEvent.ACTION_MASK
        when (action) {

            MotionEvent.ACTION_MOVE ->

                updateRating(event.x)


            MotionEvent.ACTION_UP -> {

                updateRating(event.x)

                if (hasAnimation) {

                    //call animation with callback
                    ratingAnimator?.start()
                } else {

                    //just call callback
                    if (ratingListener != null) {
                        ratingListener!!.onRatingChanged(rating + 1)
                    }
                }
            }
        }

        invalidate()
        return true
    }


    @SuppressLint("Unused")
    fun setRatingListener(ratingListener: OnRatingBarChangeListener) {
        this.ratingListener = ratingListener
    }

    private fun getDrawableByResId(@DrawableRes drawableRes: Int): Drawable? {
        return ContextCompat.getDrawable(this.context, drawableRes)
    }

    fun setRating(rating: Float) {
        this.rating = rating
        invalidate()
    }


    /*
     instantiate rating animator
     */
    private fun createAnimator(): ValueAnimator? {

        if (ratingAnimator == null) {

            ratingAnimator = ValueAnimator.ofInt(0, offsetForTransformation)
            ratingAnimator?.duration = animationDuration.toLong()
            ratingAnimator?.repeatCount = 1
            ratingAnimator?.repeatMode = ValueAnimator.REVERSE


            ratingAnimator?.interpolator = LinearInterpolator()


            // Callback that executes on animation steps.
            ratingAnimator?.addUpdateListener { animation ->


                sizeTransformedByAnimation = animation.animatedValue as Int
                invalidate()

            }

            ratingAnimator?.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animator: Animator) {}

                override fun onAnimationEnd(animator: Animator) {
                    if (ratingListener != null) {
                        ratingListener!!.onRatingChanged(rating + 1)
                    }

                    sizeTransformedByAnimation = 0
                    invalidate()
                }

                override fun onAnimationCancel(animator: Animator) {}

                override fun onAnimationRepeat(animator: Animator) {}
            })

        }
        return ratingAnimator
    }


    /*
     listener for rating changes
     */
    interface OnRatingBarChangeListener {
        fun onRatingChanged(rating: Float)
    }


    /*
      methods for save and restore
      rating bar
     */
    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        savedState.rating = rating
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)
        setRating(savedState.rating)
    }

    private class SavedState : View.BaseSavedState {
        internal var rating = 0.0f

        @TargetApi(Build.VERSION_CODES.N)
        private constructor(source: Parcel, loader: ClassLoader) : super(source, loader)

        internal constructor(superState: Parcelable) : super(superState)

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeFloat(rating)
        }
    }


}
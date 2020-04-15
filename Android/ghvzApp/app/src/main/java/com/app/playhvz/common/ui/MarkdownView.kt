/*
 * Copyright 2020 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.app.playhvz.common.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface.BOLD
import android.graphics.Typeface.ITALIC
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.util.AttributeSet
import androidx.emoji.widget.EmojiTextView

class MarkdownView : EmojiTextView {
    companion object {
        enum class TagType {
            BOLD,
            ITALIC,
            STRIKE_THROUGH
        }
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
    }

    @Override
    override fun setText(text: CharSequence?, type: BufferType?) {
        if (text.isNullOrEmpty()) {
            super.setText(text, type)
            return
        }
        var spannable: SpannableStringBuilder = SpannableStringBuilder(text)

        for (tag in TagType.values()) {
            spannable = styleTag(tag, spannable)
        }
        super.setText(spannable, BufferType.SPANNABLE)
    }

    private fun styleTag(
        tagType: TagType,
        spannable: SpannableStringBuilder
    ): SpannableStringBuilder {
        val tag = Tag(tagType)
        var startIndex = 0
        val regex: Regex = Regex(tag.getRegex())

        while (startIndex < spannable.length) {
            val result = regex.find(spannable, startIndex)
            if (result != null) {
                startIndex = Math.min(result.range.last + 1, spannable.length)
                var startTagStartInclusive = result.range.first
                if (spannable[startTagStartInclusive].isWhitespace()) {
                    // The starting space or ending space is counted in the result range, skip it
                    startTagStartInclusive++
                }
                val startTagEndExclusive = startTagStartInclusive + 2
                var endTagEndExclusive = Math.min(result.range.last + 1, spannable.length)
                if (spannable[endTagEndExclusive - 1].isWhitespace()) {
                    endTagEndExclusive--
                }
                val endTagStartInclusive = endTagEndExclusive - 2
                // Contrary to what you'd think, the spannable inclusive/exclusive tag has nothing
                // to do with the start and end index you supply, it only matters for whether text
                // inserted in those spots will be styled... the start and end indexes should always
                // be inclusive,exclusive no matter what the tag you use says.
                spannable.setSpan(
                    ForegroundColorSpan(Color.TRANSPARENT),
                    startTagStartInclusive,
                    startTagEndExclusive,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    tag.getSpanStyle(),
                    startTagEndExclusive,
                    endTagStartInclusive,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    ForegroundColorSpan(Color.TRANSPARENT),
                    endTagStartInclusive,
                    endTagEndExclusive,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            } else {
                startIndex = spannable.length + 1
            }
        }
        return spannable
    }


    class Tag(val type: TagType) {
        companion object {
            const val BOLD_REGEX_TAG = "\\*\\*"
            const val ITALIC_REGEX_TAG = "__"
            const val STRIKE_THROUGH_REGEX_TAG = "~~"
        }

        fun getRegex(): String {
            val tagString = when (type) {
                TagType.BOLD -> {
                    BOLD_REGEX_TAG
                }
                TagType.ITALIC -> {
                    ITALIC_REGEX_TAG
                }
                else -> {
                    STRIKE_THROUGH_REGEX_TAG
                }
            }
            return "$tagString\\S.*?\\S$tagString"
        }

        fun getSpanStyle(): Any {
            return when (type) {
                TagType.BOLD -> {
                    StyleSpan(BOLD)
                }
                TagType.ITALIC -> {
                    StyleSpan(ITALIC)
                }
                else -> {
                    StrikethroughSpan()
                }
            }
        }
    }
}
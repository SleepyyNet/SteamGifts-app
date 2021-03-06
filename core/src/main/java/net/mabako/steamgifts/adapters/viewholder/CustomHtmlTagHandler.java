package net.mabako.steamgifts.adapters.viewholder;

import android.app.Dialog;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.BulletSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import net.mabako.steamgifts.core.R;

import org.xml.sax.XMLReader;

import java.util.Stack;

public class CustomHtmlTagHandler implements Html.TagHandler {
    /**
     * Keeps track of lists (ol, ul). On bottom of Stack is the outermost list
     * and on top of Stack is the most nested list
     */
    Stack<String> lists = new Stack<String>();
    /**
     * Tracks indexes of ordered lists so that after a nested list ends
     * we can continue with correct index of outer list
     */
    Stack<Integer> olNextIndex = new Stack<Integer>();
    /**
     * List indentation in pixels. Nested lists use multiple of this.
     */
    private static final int indent = 10;
    private static final int listItemIndent = indent * 2;
    private static final BulletSpan bullet = new BulletSpan(indent);

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        if (tag.equalsIgnoreCase("del")) {
            processStrike(opening, output);
        } else if (tag.equalsIgnoreCase("ul")) {
            if (opening) {
                lists.push(tag);
            } else {
                lists.pop();
            }
        } else if (tag.equalsIgnoreCase("ol")) {
            if (opening) {
                lists.push(tag);
                olNextIndex.push(Integer.valueOf(1)).toString();
            } else {
                lists.pop();
                olNextIndex.pop().toString();
            }
        } else if (tag.equalsIgnoreCase("li")) {
            processListItem(opening, output);
        } else if (tag.equalsIgnoreCase("span")) {
            processSpoiler(opening, output);
        }
    }

    /**
     * Processes a single list item.
     *
     * @param opening
     * @param output
     * @see <a href="https://bitbucket.org/Kuitsi/android-textview-html-list">Kuitsi/android-textview-html-list</a>
     */
    private void processListItem(boolean opening, Editable output) {
        if (opening) {
            if (output.length() > 0 && output.charAt(output.length() - 1) != '\n') {
                output.append("\n");
            }
            String parentList = lists.peek();
            if (parentList.equalsIgnoreCase("ol")) {
                start(output, new Ol());
                output.append(olNextIndex.peek().toString() + ". ");
                olNextIndex.push(Integer.valueOf(olNextIndex.pop().intValue() + 1));
            } else if (parentList.equalsIgnoreCase("ul")) {
                start(output, new Ul());
            }
        } else {
            if (lists.peek().equalsIgnoreCase("ul")) {
                if (output.length() > 0 && output.charAt(output.length() - 1) != '\n') {
                    output.append("\n");
                }
                // Nested BulletSpans increases distance between bullet and text, so we must prevent it.
                int bulletMargin = indent;
                if (lists.size() > 1) {
                    bulletMargin = indent - bullet.getLeadingMargin(true);
                    if (lists.size() > 2) {
                        // This get's more complicated when we add a LeadingMarginSpan into the same line:
                        // we have also counter it's effect to BulletSpan
                        bulletMargin -= (lists.size() - 2) * listItemIndent;
                    }
                }
                BulletSpan newBullet = new BulletSpan(bulletMargin);
                end(output,
                        Ul.class,
                        new LeadingMarginSpan.Standard(listItemIndent * (lists.size() - 1)),
                        newBullet);
            } else if (lists.peek().equalsIgnoreCase("ol")) {
                if (output.length() > 0 && output.charAt(output.length() - 1) != '\n') {
                    output.append("\n");
                }
                int numberMargin = listItemIndent * (lists.size() - 1);
                if (lists.size() > 2) {
                    // Same as in ordered lists: counter the effect of nested Spans
                    numberMargin -= (lists.size() - 2) * listItemIndent;
                }
                end(output,
                        Ol.class,
                        new LeadingMarginSpan.Standard(numberMargin));
            }
        }
    }

    private void processStrike(boolean opening, Editable output) {
        int len = output.length();
        if (opening) {
            output.setSpan(new StrikethroughSpan(), len, len, Spannable.SPAN_MARK_MARK);
        } else {
            Object obj = getLast(output, StrikethroughSpan.class);
            int where = output.getSpanStart(obj);

            output.removeSpan(obj);

            if (where != len) {
                output.setSpan(new StrikethroughSpan(), where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private void processSpoiler(boolean opening, Editable output) {
        int len = output.length();
        if (opening) {
            output.setSpan(new Spoiler(), len, len, Spannable.SPAN_MARK_MARK);
        } else {
            Object obj = getLast(output, Spoiler.class);
            int where = output.getSpanStart(obj);

            output.removeSpan(obj);

            if (where != len) {
                char[] str = new char[len - where];
                output.getChars(where, len, str, 0);
                final String text = String.valueOf(str);

                output.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        Dialog dialog = new Dialog(widget.getContext());
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.spoiler_dialog);
                        ((TextView) dialog.findViewById(R.id.text)).setText(text);
                        dialog.show();
                    }
                }, where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                output.setSpan(new ForegroundColorSpan(0xff666666), where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                output.setSpan(new BackgroundColorSpan(0xff666666), where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    /**
     * @see android.text.Html
     */
    private static void start(Editable text, Object mark) {
        int len = text.length();
        text.setSpan(mark, len, len, Spanned.SPAN_MARK_MARK);
    }

    /**
     * Modified from {@link android.text.Html}
     */
    private static void end(Editable text, Class<?> kind, Object... replaces) {
        int len = text.length();
        Object obj = getLast(text, kind);
        int where = text.getSpanStart(obj);
        text.removeSpan(obj);
        if (where != len) {
            for (Object replace : replaces) {
                text.setSpan(replace, where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return;
    }

    /**
     * @see android.text.Html
     */
    private static Object getLast(Spanned text, Class<?> kind) {
        /*
         * This knows that the last returned object from getSpans()
		 * will be the most recently added.
		 */
        Object[] objs = text.getSpans(0, text.length(), kind);
        if (objs.length == 0) {
            return null;
        }
        return objs[objs.length - 1];
    }

    private static class Ul {
    }

    private static class Ol {
    }

    private static class Spoiler {
    }
}

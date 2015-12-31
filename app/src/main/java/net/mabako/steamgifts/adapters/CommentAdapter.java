package net.mabako.steamgifts.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.data.Comment;

import java.util.List;

/**
 * Adapter to hold comments for a giveaway/discussion.
 */
public class CommentAdapter extends EndlessAdapter<Comment, CommentAdapter.ViewHolder> {
    private static final int ITEMS_PER_PAGE = 25;
    private final int[] colors = {android.R.color.holo_blue_dark, android.R.color.holo_green_dark, android.R.color.holo_orange_dark, android.R.color.holo_red_dark};

    private float displayDensity;

    public CommentAdapter(Context context, RecyclerView view, EndlessAdapter.OnLoadListener loadListener) {
        super(view, loadListener);
        for(int i = 0; i < colors.length; ++ i)
            colors[i] = ContextCompat.getColor(context, colors[i]);
    }

    @Override
    public ViewHolder onCreateActualViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindActualViewHolder(ViewHolder holder, int position) {
        Comment comment = getItem(position);

        holder.commentAuthor.setText(comment.getAuthor());
        holder.commentTime.setText(comment.getTimeAgo());

        CharSequence desc = Html.fromHtml(comment.getContent());
        desc = desc.subSequence(0, desc.length() - 2);
        holder.commentContent.setText(desc);

        // Space before the marker
        ViewGroup.LayoutParams params = holder.commentIndent.getLayoutParams();
        params.width = holder.commentMarker.getLayoutParams().width * comment.getDepth();
        holder.commentIndent.setLayoutParams(params);

        // Marker
        holder.commentMarker.setBackgroundColor(colors[comment.getDepth() % colors.length]);
        holder.commentMarker.invalidate();
    }

    @Override
    protected boolean hasEnoughItems(List<Comment> items) {
        if(items.size() < ITEMS_PER_PAGE)
            return false;

        int rootLevelComments = 0;
        for(Comment comment : items)
            if(comment.getDepth() == 0)
                ++ rootLevelComments;

        return rootLevelComments == ITEMS_PER_PAGE;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView commentAuthor;
        private final TextView commentTime;
        private final TextView commentContent;
        private final View commentIndent;
        private final View commentMarker;

        public ViewHolder(View v) {
            super(v);
            commentAuthor = (TextView) v.findViewById(R.id.user);
            commentTime = (TextView) v.findViewById(R.id.time);
            commentContent = (TextView) v.findViewById(R.id.content);
            commentMarker = v.findViewById(R.id.comment_marker);
            commentIndent = v.findViewById(R.id.comment_indent);

            commentContent.setMovementMethod(LinkMovementMethod.getInstance());

            // OnClickListener...
        }
    }
}
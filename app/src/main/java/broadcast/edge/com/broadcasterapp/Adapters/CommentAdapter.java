package broadcast.edge.com.broadcasterapp.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import broadcast.edge.com.broadcasterapp.Models.Comments;
import broadcast.edge.com.broadcasterapp.R;


/**
 * Created by Ashish on 19-01-2018.
 */

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyViewHolder> {
    List<Comments> comments;
    Context context;

    public CommentAdapter(Context context, List<Comments> comments){
        this.context = context;
        this.comments = comments;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tv_comments,parent,false);
        MyViewHolder viewHolder = new MyViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.tvUserName.setText(comments.get(position).getUsername() + ": ");
        holder.tvComment.setText(comments.get(position).getCommnets());
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvComment;
        public MyViewHolder(View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_username);
            tvComment = itemView.findViewById(R.id.tv_comment);
        }
    }
}

package net.mabako.steamgifts.fragments.profile;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

import net.mabako.steamgifts.adapters.GiveawayAdapter;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.Game;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.fragments.ListFragment;
import net.mabako.steamgifts.fragments.interfaces.IActivityTitle;
import net.mabako.steamgifts.tasks.LoadGameListTask;
import net.mabako.steamgifts.tasks.Utils;

import org.jsoup.nodes.Element;

import java.io.Serializable;
import java.util.List;

public class WonListFragment extends ListFragment<GiveawayAdapter> implements IActivityTitle {
    public WonListFragment() {
        loadItemsInitially = false;
    }

    @Override
    public int getTitleResource() {
        return R.string.user_giveaway_won;
    }

    @Override
    public String getExtraTitle() {
        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter.setFragmentValues(getActivity(), this, null);
    }

    @Override
    protected GiveawayAdapter createAdapter() {
        return new GiveawayAdapter(50, PreferenceManager.getDefaultSharedPreferences(getContext()));
    }

    @Override
    protected AsyncTask<Void, Void, ?> getFetchItemsTask(int page) {
        return new LoadGameListTask(this, "giveaways/won", page, null) {
            @Override
            protected IEndlessAdaptable load(Element element) {
                Element firstColumn = element.select(".table__column--width-fill").first();
                Element link = firstColumn.select("a.table__column__heading").first();

                Uri linkUri = Uri.parse(link.attr("href"));
                String giveawayLink = linkUri.getPathSegments().get(1);
                String giveawayName = linkUri.getPathSegments().get(2);

                Giveaway giveaway = new Giveaway(giveawayLink);
                giveaway.setName(giveawayName);
                giveaway.setTitle(link.text());


                Element image = element.select(".global__image-inner-wrap").first();
                if (image != null) {
                    Uri uri = Uri.parse(Utils.extractAvatar(image.attr("style")));
                    List<String> pathSegments = uri.getPathSegments();
                    if (pathSegments.size() >= 3) {
                        giveaway.setGameId(Integer.parseInt(pathSegments.get(2)));
                        giveaway.setType("apps".equals(pathSegments.get(1)) ? Game.Type.APP : Game.Type.SUB);
                    }
                }

                giveaway.setPoints(-1);
                giveaway.setEntries(-1);
                giveaway.setTimeRemaining(firstColumn.select("span").text());

                // Has any feedback option been picked yet?
                // If so, this would be == 1, 0 hidden items implies both feedback options are currently available to be picked.
                giveaway.setEntered(element.select(".table__gift-feedback-awaiting-reply.is-hidden").size() == 0);

                return giveaway;
            }
        };
    }

    @Override
    protected Serializable getType() {
        return null;
    }
}

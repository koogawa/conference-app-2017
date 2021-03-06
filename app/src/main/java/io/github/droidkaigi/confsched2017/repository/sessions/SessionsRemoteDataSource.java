package io.github.droidkaigi.confsched2017.repository.sessions;

import com.annimon.stream.Stream;

import android.text.TextUtils;

import java.util.List;

import javax.inject.Inject;

import io.github.droidkaigi.confsched2017.api.DroidKaigiClient;
import io.github.droidkaigi.confsched2017.model.Session;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public final class SessionsRemoteDataSource implements SessionsDataSource {

    private final DroidKaigiClient client;

    @Inject
    public SessionsRemoteDataSource(DroidKaigiClient client) {
        this.client = client;
    }

    @Override
    public Single<List<Session>> findAll(String languageId) {
        return client.getSessions(languageId)
                .map(sessions -> {
                    // API returns some sessions which have empty room info.
                    for (Session session : sessions) {
                        if (session.room != null && TextUtils.isEmpty(session.room.name)) {
                            session.room = null;
                        }
                    }
                    return sessions;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Maybe<Session> find(int sessionId, String languageId) {
        return findAll(languageId).map(sessions -> Stream.of(sessions)
                .filter(session -> session.id == sessionId)
                .findSingle()
                .get()).toMaybe();
    }

    @Override
    public void updateAllAsync(List<Session> sessions) {
        // Do nothing
    }

}

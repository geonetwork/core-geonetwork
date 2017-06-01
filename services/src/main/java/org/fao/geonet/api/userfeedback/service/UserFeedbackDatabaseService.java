package org.fao.geonet.api.userfeedback.service;

import org.fao.geonet.domain.User;
import org.fao.geonet.domain.userfeedback.Rating;
import org.fao.geonet.domain.userfeedback.UserFeedback;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * User feedback provider that uses database persistence.
 *
 * @author Jose García
 */
@Service
public class UserFeedbackDatabaseService implements IUserFeedbackService {

    @Override
    public List<UserFeedback> retrieveUserFeedbackForMetadata(String metadataUuid) {
        return list;
    }

    @Override
    public UserFeedback retrieveUserFeedback(String feedbackUuid) {
        return list.get(0);
    }

    @Override
    public Rating retrieveMetadataRating(String feedbackUuid) {
        return rating;
    }

    @Override
    public void publishUserFeedback(String feedbackUuid, User user) {

    }

    @Override
    public void saveUserFeedback(UserFeedback userFeedback) {

    }

    @Override
    public void removeUserFeedback(String feedbackUuid) {

    }

    // TODO: REMOVE Mockup data
    private static List<UserFeedback> list = new ArrayList<UserFeedback>();

    private static Rating rating = new Rating();

    static {
        UserFeedback uf1;
        list.add(uf1 = new UserFeedback());
        uf1.setUuid("lalalala");
        uf1.setComment("Lorem ipsum dolor sit amet, consectetur adipiscing elit."
            + " Etiam ultrices ligula urna. Ut cursus, mauris sed auctor"
            + " accumsan, quam ligula gravida lectus, ut condimentum velit sem "
            + "a risus.");
        User u1;
        uf1.setUser(u1 = new User());
        u1.setName("Marco Polo");
        u1.setOrganisation("SomethingGeo");

        UserFeedback uf2;
        list.add(uf2 = new UserFeedback());
        uf2.setUuid("lalalalaro");
        uf2.setComment("Lorem ipsum dolor sit amet, consectetur adipiscing elit."
            + " Etiam ultrices ligula urna. Ut cursus, mauris sed auctor"
            + " accumsan, quam ligula gravida lectus, ut condimentum velit sem "
            + "a risus.");
        User u2;
        uf2.setUser(u2 = new User());
        u2.setName("Cristoforo Colombo");
        u2.setOrganisation("GeoWhathever");

        UserFeedback uf3;
        list.add(uf3 = new UserFeedback());
        uf3.setUuid("lalalalababooo");
        uf3.setComment("Indeed");
        uf3.setUser(u1);

        UserFeedback uf4;
        list.add(uf4 = new UserFeedback());
        uf4.setUuid("lalalalawewewewewe");
        uf4.setComment("Lorem ipsum dolor sit amet, consectetur adipiscing elit."
            + " Etiam ultrices ligula urna. Ut cursus, mauris sed auctor"
            + " accumsan, quam ligula gravida lectus, ut condimentum velit sem "
            + "a risus.");
        uf4.setUser(u2);

        UserFeedback uf5;
        list.add(uf5 = new UserFeedback());
        uf5.setUuid("lalalalagogogogogo");
        uf5.setComment("Yes, you're right");
        uf5.setUser(u1);

        UserFeedback uf6;
        list.add(uf6 = new UserFeedback());
        uf6.setUuid("lalalalaqaaqaqaqaqaa");
        uf6.setComment("Lorem ipsum dolor sit amet, consectetur adipiscing elit."
            + " Etiam ultrices ligula urna. Ut cursus, mauris sed auctor"
            + " accumsan, quam ligula gravida lectus, ut condimentum velit sem "
            + "a risus.");
        uf6.setUser(u2);




        rating.setAvgRating(4);

        rating.setCommentsCount(list.size());


    }


    // ************************
}

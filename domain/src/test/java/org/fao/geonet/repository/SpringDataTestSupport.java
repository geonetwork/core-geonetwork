package org.fao.geonet.repository;

import com.google.common.base.Function;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static org.junit.Assert.*;

public final class SpringDataTestSupport {

    private SpringDataTestSupport() {
    }

    public static void setId(ReservedOperation view, int normalId) throws Exception {
        Field declaredField = view.getClass().getDeclaredField("_id");
        declaredField.setAccessible(true);
        declaredField.set(view, normalId);
    }

    public static void setId(ReservedGroup group, int normalId) throws Exception {
        Field declaredField = group.getClass().getDeclaredField("_id");
        declaredField.setAccessible(true);
        declaredField.set(group, normalId);
    }

}

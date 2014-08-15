package com.trein.gtfs.mongo.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.trein.gtfs.mongo.MongoRepositoryConfig;
import com.trein.gtfs.mongo.entities.Shape;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MongoRepositoryConfig.class })
@SuppressWarnings("boxing")
public class ShapeRepositoryTest {
    
    @Autowired
    private ShapeRepository repository;
    
    @Test
    public void readsFirstPageCorrectly() {
        Page<Shape> entities = this.repository.findAll(new PageRequest(0, 10));
        assertThat(entities.isFirst(), is(true));
    }
    
    @Test
    public void queryLocation() {
        Point center = new Point(-30.030277, -51.230339);
        Distance radius = new Distance(1, Metrics.KILOMETERS);
        GeoResults<Shape> entities = this.repository.findByLocationNear(center, radius);
        assertThat(entities, is(notNullValue()));
    }
}

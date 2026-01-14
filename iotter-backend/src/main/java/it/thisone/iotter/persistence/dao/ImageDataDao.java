package it.thisone.iotter.persistence.dao;


import org.springframework.stereotype.Repository;

import it.thisone.iotter.persistence.ifc.IImageDataDao;
import it.thisone.iotter.persistence.model.ImageData;

@Repository
public class ImageDataDao extends BaseEntityDao<ImageData> implements IImageDataDao {
	public ImageDataDao() {
        super();
        setClazz(ImageData.class);
    }
}

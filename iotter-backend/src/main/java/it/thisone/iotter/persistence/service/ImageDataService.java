package it.thisone.iotter.persistence.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.thisone.iotter.persistence.ifc.IImageDataDao;
import it.thisone.iotter.persistence.model.ImageData;

@Service
public class ImageDataService {
	@Autowired
	private IImageDataDao dao;

	public ImageDataService() {
		super();
	}

	// API

	@Transactional
	public void create(ImageData entity) {
		dao.create(entity);
	}

	@Transactional
	public void update(ImageData entity) {
		dao.update(entity);
	}

	public ImageData findOne(String id ) {
		return dao.findOne(id);
	}

	public List<ImageData> findAll() {
		return dao.findAll();
	}

	@Transactional
	public void deleteById(String entityId) {
		dao.deleteById(entityId);
	}

}

package it.thisone.iotter.persistence.service;

import java.text.ChoiceFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.ifc.IMeasureUnitTypeDao;
import it.thisone.iotter.persistence.model.MeasureUnitType;

@Service
public class MeasureUnitTypeService {

    @Autowired
    private IMeasureUnitTypeDao dao;

    public MeasureUnitTypeService() {
        super();
    }

    @Transactional
    public void create(MeasureUnitType entity) {
        dao.create(entity);
    }

    @Transactional
    public void update(MeasureUnitType entity) {
        dao.update(entity);
    }

    public MeasureUnitType findOne(String id ) {
        return dao.findOne(id);
    }

    public List<MeasureUnitType> findAll() {
        return dao.findAll();
    }
    

    @Transactional
    public void deleteById(String entityId ){
    	dao.deleteById(entityId);
    }
    
    public MeasureUnitType findByCode(int code){
    	try {
			return dao.findByCode(code);
		} catch (BackendServiceException e) {
		}
    	return null;
    }
    
    /*
    ChoiceFormat needed for formatting data with enums
    */
    public ChoiceFormat getMeasureUnitChoiceFormat() {
		List<MeasureUnitType> units = this.findAll();
		Collections.sort(units, new Comparator<MeasureUnitType>() {
			@Override
			public int compare(MeasureUnitType o1, MeasureUnitType o2) {
				return o1.getCode().compareTo(o2.getCode());
			}
		});
		List<String> values = new ArrayList<>();
		for (MeasureUnitType unit : units) {
			// '<' || ch == '#' || ch == '\u2264'
			String name = unit.getName();
			name = name.replaceAll("<", "");
			name = name.replaceAll("#", "");
			name = name.replaceAll("\u2264", "");
			values.add(String.format("%d#%s", unit.getCode(), name));
		}
        String pattern= StringUtils.join(values, "|");
        return new ChoiceFormat(pattern);
		
	}
    
    
}

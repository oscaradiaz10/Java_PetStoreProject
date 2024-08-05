package pet.store.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pet.store.controller.model.PetStoreData;
import pet.store.dao.PetStoreDao;
import pet.store.entity.PetStore;

@Service
public class PetStoreService {
	
	@Autowired
	private PetStoreDao petStoreDao;
	
	@Transactional(readOnly = false)
	public PetStoreData savePetStore(PetStoreData petStoreData) {
		PetStore petStore = findOrCreatePetStore(petStoreData.getStoreId());
		copyPetStoreFields(petStore, petStoreData);
		petStore = petStoreDao.save(petStore);
		return new PetStoreData(petStore);
    }
	
	private PetStore findOrCreatePetStore(Long petStoreId) {
        if (petStoreId == null) {
            return new PetStore();
        } else {
            return findPetStoreById(petStoreId);
        }
    }

    private PetStore findPetStoreById(Long petStoreId) {
        return petStoreDao.findById(petStoreId).orElseThrow(
        	() -> new NoSuchElementException("Pet store with ID=" + petStoreId + " not found"));
    }

    private void copyPetStoreFields(PetStore petStore, PetStoreData petStoreData) {
        petStore.setStoreName(petStoreData.getStoreName());
        petStore.setStoreAddress(petStoreData.getStoreAddress());
        petStore.setStoreCity(petStoreData.getStoreCity());
        petStore.setStoreState(petStoreData.getStoreState());
        petStore.setStoreZip(petStoreData.getStoreZip());
        petStore.setStorePhone(petStoreData.getStorePhone());
    }
    
    @Transactional(readOnly = true)
	public List<PetStoreData> retrieveAllPetStores() {
//    	List<PetStore> petStores = petStoreDao.findAll();
//    	List<PetStoreData> response = new LinkedList<>();
//    	
//    	for (PetStore petStore : petStores) {
//    		response.add(new PetStoreData(petStore));
//    	}
//    	
//    	return response;
    	
    	return petStoreDao.findAll().stream().map(PetStoreData::new).toList();
	}
}

package pet.store.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pet.store.controller.model.PetStoreData;
import pet.store.controller.model.PetStoreData.PetStoreCustomer;
import pet.store.controller.model.PetStoreData.PetStoreEmployee;
import pet.store.dao.CustomerDao;
import pet.store.dao.EmployeeDao;
import pet.store.dao.PetStoreDao;
import pet.store.entity.Customer;
import pet.store.entity.Employee;
import pet.store.entity.PetStore;

@Service
public class PetStoreService {
	
	@Autowired
	private PetStoreDao petStoreDao;
	
	@Autowired
    private EmployeeDao employeeDao;
	
	@Autowired
	private CustomerDao customerDao;
		
	@Transactional(readOnly = false)
	public PetStoreData savePetStore(PetStoreData petStoreData) {
		Long petStoreId = petStoreData.getPetStoreId();
		PetStore petStore = findOrCreatePetStore(petStoreId);
		
		copyPetStoreFields(petStore, petStoreData);
		return new PetStoreData(petStoreDao.save(petStore));
    }
	
	private void copyPetStoreFields(PetStore petStore, PetStoreData petStoreData) {
        petStore.setPetStoreAddress(petStoreData.getPetStoreAddress());
        petStore.setPetStoreCity(petStoreData.getPetStoreCity());
        petStore.setPetStoreId(petStoreData.getPetStoreId());
        petStore.setPetStoreName(petStoreData.getPetStoreName());
        petStore.setPetStorePhone(petStoreData.getPetStorePhone());
        petStore.setPetStoreState(petStoreData.getPetStoreState());
        petStore.setPetStoreZip(petStoreData.getPetStoreZip());
    }
	
	private PetStore findOrCreatePetStore(Long petStoreId) {
        if(Objects.isNull(petStoreId)) {
            return new PetStore();
        } else {
            return findPetStoreById(petStoreId);
        }
    }

    private PetStore findPetStoreById(Long petStoreId) {
        return petStoreDao.findById(petStoreId).orElseThrow(
        	() -> new NoSuchElementException("Pet store with ID=" + petStoreId + " not found"));
    }
    
    @Transactional(readOnly = true)
    public List<PetStoreData> retrieveAllPetStores() {
        List<PetStoreData> petStoreDataList = petStoreDao.findAll().stream()
                .map(PetStoreData::new)
                .collect(Collectors.toList());

        for (PetStoreData petStoreData : petStoreDataList) {
            petStoreData.getCustomers().clear();
            petStoreData.getEmployees().clear();
        }

        return petStoreDataList;
    }
   
    public Employee findEmployeeById(Long petStoreId, Long employeeId) {
        Employee employee = employeeDao.findById(employeeId)
                .orElseThrow(() -> new NoSuchElementException("Employee with ID=" + employeeId + " not found"));

        if(!employee.getPetStore().getPetStoreId().equals(petStoreId)) {
            throw new IllegalArgumentException("Employee does not belong to the Pet Store with ID=" + petStoreId);
        }

        return employee;
    }
    
    public Employee findOrCreateEmployee(Long employeeId, Long petStoreId) {
        if(employeeId == null) {
            return new Employee();
        } else {
            return findEmployeeById(employeeId, petStoreId);
        }
    }
    
    public void copyEmployeeFields(Employee employee, PetStoreEmployee petStoreEmployee) {
        employee.setEmployeeFirstName(petStoreEmployee.getEmployeeFirstName());
        employee.setEmployeeLastName(petStoreEmployee.getEmployeeLastName());
        employee.setEmployeeJobTitle(petStoreEmployee.getEmployeeJobTitle());
    }
    
    @Transactional(readOnly = false)
    public PetStoreEmployee saveEmployee(Long petStoreId, PetStoreEmployee petStoreEmployee) {
        PetStore petStore = findPetStoreById(petStoreId);

        Employee employee = findOrCreateEmployee(petStoreEmployee.getEmployeeId(), petStoreId);
        
        copyEmployeeFields(employee, petStoreEmployee);
        
        employee.setPetStore(petStore);
        
        petStore.getEmployees().add(employee);
        employee = employeeDao.save(employee);
        
        petStoreEmployee.setEmployeeId(employee.getEmployeeId());

        return petStoreEmployee;
    }
    
    public void copyCustomerFields(Customer customer, PetStoreCustomer petStoreCustomer) {
        customer.setCustomerFirstName(petStoreCustomer.getCustomerFirstName());
        customer.setCustomerLastName(petStoreCustomer.getCustomerLastName());
        customer.setCustomerEmail(petStoreCustomer.getCustomerEmail());
    }
    
    @Transactional(readOnly = false)
    public PetStoreCustomer saveCustomer(Long petStoreId, PetStoreCustomer petStoreCustomer) {
        PetStore petStore = findPetStoreById(petStoreId);

        Customer customer = findOrCreateCustomer(petStoreCustomer.getCustomerId(), petStoreId);

        copyCustomerFields(customer, petStoreCustomer);

        if (!customer.getPetStores().contains(petStore)) {
            customer.getPetStores().add(petStore);
        }
        
        petStore.getCustomers().add(customer);
        customer = customerDao.save(customer);

        petStoreCustomer.setCustomerId(customer.getCustomerId());

        return petStoreCustomer;
    }

	public Customer findOrCreateCustomer(Long customerId, Long petStoreId) {
		if(customerId == null) {
            return new Customer();
        } else {
            return findCustomerById(customerId, petStoreId);
        }
	}
	
	public Customer findCustomerById(Long petStoreId, Long customerId) {
        Customer customer = customerDao.findById(customerId)
                .orElseThrow(() -> new NoSuchElementException("Customer with ID=" + customerId + " not found"));

        boolean belongsToPetStore = customer.getPetStores().stream()
                .anyMatch(petStore -> petStore.getPetStoreId().equals(petStoreId));

        if (!belongsToPetStore) {
            throw new IllegalArgumentException("Customer does not belong to the Pet Store with ID=" + petStoreId);
        }

        return customer;
    }
	
	@Transactional(readOnly = false)
    public void deletePetStoreById(Long petStoreId) {
        PetStore petStore = findPetStoreById(petStoreId);
        petStoreDao.delete(petStore);
    }
}



package com.hand.demo.service;

import javax.security.auth.login.CredentialException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.hand.demo.model.Dtos.appuser_dtos.GetUpdatedAppUserDto;
import com.hand.demo.model.Dtos.appuser_dtos.UpdateAppUserDto;
import com.hand.demo.model.entity.Customer;
import com.hand.demo.repository.CustomerRepository;

@Service
public class CustomerService  extends AppUserService {

    @Autowired
    private CustomerRepository customerRepository;

    // ############################
    // #### Customer Operations ###
    // ############################
    public Customer findCustomerById(Long id) {
        return customerRepository.findById(id).orElseThrow(() -> new RuntimeException("Customer not found"));
    }


    public Customer updateCustomerHelper(UpdateAppUserDto customerDto) throws CredentialException {
        UserDetails appUser = super.userAuthorization();
        Customer customer = customerRepository.findByUsername(appUser.getUsername()).get();
        customer.updateDtoToAppUser(customerDto);

        return customer;
    }

    public GetUpdatedAppUserDto updateCustomer(UpdateAppUserDto customerDto) throws CredentialException {

        Customer customer = updateCustomerHelper(customerDto);
        if (customerDto.getMediaId() != null) {
            appUserImageService.setAvatar(customer, customerDto.getMediaId());
        }
        GetUpdatedAppUserDto updateAppUserDto = new GetUpdatedAppUserDto(customerRepository.save(customer));
        return updateAppUserDto;
    }


}

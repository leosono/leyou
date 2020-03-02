package com.leyou.order.client;

import com.leyou.order.dto.AddressDTO;

import java.util.ArrayList;
import java.util.List;

public abstract class AddressClient {

    public static final List<AddressDTO> addressList = new ArrayList<AddressDTO>(){
        {
            AddressDTO address = new AddressDTO();
            address.setId(1L);
            address.setState("上海");
            address.setCity("上海");
            address.setDistrict("浦东新区");
            address.setAddress("航头镇航头路18号光明大厦 3号楼");
            address.setName("虎哥");
            address.setPhone("15849233995");
            address.setZipCode("210000");
            address.setIsDefault(true);
            add(address);

           /* AddressDTO address2 = new AddressDTO();
            address.setId(2L);
            address.setState("北京");
            address.setCity("北京");
            address.setDistrict("朝阳区");
            address.setAddress("天堂路 3号楼");
            address.setName("轩哥");
            address.setPhone("13629836649");
            address.setZipCode("100000");
            address.setIsDefault(false);
            add(address2);*/
        }
    };

    public static AddressDTO findById(Long id){
        for (AddressDTO addressDTO : addressList) {
            if(addressDTO.getId().equals(id)){
               return addressDTO;
            }
        }
        return null;
    }
}

package com.example.loanApplication.config;

import com.example.loanApplication.dto.EmiScheduleResponseDto;
import com.example.loanApplication.entity.EmiSchedules;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper(){

        return new ModelMapper();
    }
}






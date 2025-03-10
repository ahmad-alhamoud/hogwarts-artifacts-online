package com.ahmad.hogwartsartifactsonline.wizard;

import com.ahmad.hogwartsartifactsonline.system.Result;
import com.ahmad.hogwartsartifactsonline.system.StatusCode;
import com.ahmad.hogwartsartifactsonline.wizard.converter.WizardDtoToWizardConverter;
import com.ahmad.hogwartsartifactsonline.wizard.converter.WizardToWizardDtoConverter;
import com.ahmad.hogwartsartifactsonline.wizard.dto.WizardDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.endpoint.base-url}/wizards")
public class WizardController {

    private final WizardService wizardService;
    private final WizardToWizardDtoConverter wizardToWizardDtoConverter;
    private final WizardDtoToWizardConverter wizardDtoToWizardConverter;

    public WizardController(WizardService wizardService, WizardToWizardDtoConverter wizardToWizardDtoConverter, WizardDtoToWizardConverter wizardDtoToWizardConverter) {
        this.wizardService = wizardService;
        this.wizardToWizardDtoConverter = wizardToWizardDtoConverter;
        this.wizardDtoToWizardConverter = wizardDtoToWizardConverter;
    }


    @GetMapping
    public Result findAllWizard() {
        List<Wizard> wizards = wizardService.findall();
        List<WizardDto> wizardDtos = wizards.stream()
                .map(wizardToWizardDtoConverter::convert)
                .toList();

        return new Result(true, StatusCode.SUCCESS, "Find All Wizard Success", wizardDtos);
    }

    @PostMapping
    public Result addWizard(@RequestBody @Valid WizardDto wizardDto) {
        Wizard wizard = wizardDtoToWizardConverter.convert(wizardDto);
        Wizard savedWizard = wizardService.add(wizard);
        WizardDto updatedDto = wizardToWizardDtoConverter.convert(savedWizard);

        return new Result(true,StatusCode.SUCCESS,"Add Wizard Success",updatedDto);
    }

    @GetMapping("/{wizardId}")
    public Result findWizardById(@PathVariable Integer wizardId) {
        Wizard foundWizard = wizardService.findById(wizardId);
        WizardDto wizardDto = wizardToWizardDtoConverter.convert(foundWizard);
        return new Result(true, StatusCode.SUCCESS, "Find Wizard Success", wizardDto);
    }

    @PutMapping("/{wizardId}")
    public Result updateWizardById(@PathVariable Integer wizardId, @RequestBody @Valid WizardDto wizardDto) {

        Wizard newWizard = wizardDtoToWizardConverter.convert(wizardDto);
        Wizard updatedWizard = wizardService.update(wizardId, newWizard);
        WizardDto updatedDto = wizardToWizardDtoConverter.convert(updatedWizard);
        return new Result(true, StatusCode.SUCCESS, "Update Wizard Success", updatedDto);


    }


    @DeleteMapping("/{wizardId}")
    public Result deleteWizardById(@PathVariable Integer wizardId) {
        wizardService.deleteWizardById(wizardId);
        return new Result(true, StatusCode.SUCCESS, "Delete Wizard Success");
    }
}

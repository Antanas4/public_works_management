import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
    name: 'camelCaseToTitle',
    standalone: false
})
export class CamelCaseToTitlePipe implements PipeTransform {

    transform(value: string): string {
        if (!value) return '';
        const spaced = value.replace(/([A-Z])/g, ' $1').toLowerCase();
        return spaced.charAt(0).toUpperCase() + spaced.slice(1);
    }
}

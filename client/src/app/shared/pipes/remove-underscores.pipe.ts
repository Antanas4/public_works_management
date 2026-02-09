import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
    name: 'removeUnderscores',
    standalone: false
})
export class RemoveUnderscoresPipe implements PipeTransform {
    transform(value: string): string {
      return value.replace(/_/g, ' ');
    }
}

import dayjs from 'dayjs/esm';
import { IUser } from 'app/entities/user/user.model';

export interface INews {
  id?: number;
  name?: string | null;
  description?: string | null;
  category?: string | null;
  createdDate?: dayjs.Dayjs | null;
  author?: IUser | null;
}

export class News implements INews {
  constructor(
    public id?: number,
    public name?: string | null,
    public description?: string | null,
    public category?: string | null,
    public createdDate?: dayjs.Dayjs | null,
    public author?: IUser | null
  ) {}
}

export function getNewsIdentifier(news: INews): number | undefined {
  return news.id;
}

export interface ApiErrorNormalized {
  status: number;
  message: string;
  path: string | null;
  errors: unknown[] | null;
  timestamp: string | null;
  error?: string; 
}
